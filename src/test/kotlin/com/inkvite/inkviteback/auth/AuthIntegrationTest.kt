package com.inkvite.inkviteback.auth

import com.inkvite.inkviteback.TestcontainersConfiguration
import com.inkvite.inkviteback.artist.entity.TattooArtist
import com.inkvite.inkviteback.artist.repository.TattooArtistRepository
import com.inkvite.inkviteback.auth.dto.LoginRequestDto
import com.inkvite.inkviteback.auth.dto.LogoutRequestDto
import com.inkvite.inkviteback.auth.dto.RefreshRequestDto
import com.inkvite.inkviteback.auth.dto.RegisterRequestDto
import com.inkvite.inkviteback.auth.entity.RefreshToken
import com.inkvite.inkviteback.auth.entity.VerificationToken
import com.inkvite.inkviteback.auth.repository.RefreshTokenRepository
import com.inkvite.inkviteback.auth.repository.VerificationTokenRepository
import com.inkvite.inkviteback.auth.service.JwtService
import com.inkvite.inkviteback.email.service.EmailService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.assertj.core.api.Assertions.assertThat
import java.time.Instant
import java.util.UUID
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.ObjectMapper

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration::class)
class AuthIntegrationTest {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var objectMapper: ObjectMapper
    @Autowired lateinit var tokenRepository: VerificationTokenRepository
    @Autowired lateinit var artistRepository: TattooArtistRepository
    @Autowired lateinit var refreshTokenRepository: RefreshTokenRepository
    @Autowired lateinit var passwordEncoder: PasswordEncoder
    @Autowired lateinit var jwtService: JwtService

    @MockitoBean lateinit var emailService: EmailService

    @BeforeEach
    fun cleanup() {
        tokenRepository.deleteAll()
        refreshTokenRepository.deleteAll()
        artistRepository.deleteAll()
    }

    @Test
    fun `register creates inactive artist and sends verification email`() {
        mockMvc.perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(RegisterRequestDto("artist@test.com", "password123")))
        ).andExpect(status().isNoContent)

        val artist = artistRepository.findAll().single()
        assertThat(artist.email).isEqualTo("artist@test.com")
        assertThat(artist.registeredAt).isNotNull()
        assertThat(artist.activatedAt).isNull()

        val token = tokenRepository.findAll().single()
        verify(emailService).sendVerificationEmail("artist@test.com", token.token)
    }

    @Test
    fun `register with duplicate email returns 409`() {
        val body = objectMapper.writeValueAsString(RegisterRequestDto("artist@test.com", "password123"))
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isConflict)
    }

    @Test
    fun `verify with valid token activates artist and deletes token`() {
        mockMvc.perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(RegisterRequestDto("artist@test.com", "password123")))
        )
        val token = tokenRepository.findAll().single().token

        mockMvc.perform(get("/auth/verify").param("token", token))
            .andExpect(status().isNoContent)

        assertThat(artistRepository.findAll().single().activatedAt).isNotNull()
        assertThat(tokenRepository.findAll()).isEmpty()
    }

    @Test
    fun `verify with expired token returns 400 and deletes token`() {
        val artistId = UUID.randomUUID()
        artistRepository.save(TattooArtist(id = artistId, email = "artist@test.com", password = "hash", registeredAt = Instant.now()))
        tokenRepository.save(VerificationToken(token = "expired-token", tattooArtistId = artistId, expiresAt = Instant.now().minusSeconds(1)))

        mockMvc.perform(get("/auth/verify").param("token", "expired-token"))
            .andExpect(status().isBadRequest)

        assertThat(tokenRepository.findAll()).isEmpty()
    }

    @Test
    fun `verify with unknown token returns 404`() {
        mockMvc.perform(get("/auth/verify").param("token", "unknown-token"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `register with invalid email returns 400`() {
        val body = objectMapper.writeValueAsString(RegisterRequestDto("not-an-email", "password123"))
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `register with too short password returns 400`() {
        val body = objectMapper.writeValueAsString(RegisterRequestDto("artist@test.com", "short"))
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `resend verification replaces existing token and sends new email`() {
        mockMvc.perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(RegisterRequestDto("artist@test.com", "password123")))
        )
        val firstToken = tokenRepository.findAll().single().token

        mockMvc.perform(post("/auth/resend-verification").param("email", "artist@test.com"))
            .andExpect(status().isNoContent)

        val newToken = tokenRepository.findAll().single().token
        assertThat(newToken).isNotEqualTo(firstToken)
        verify(emailService).sendVerificationEmail("artist@test.com", newToken)
    }

    @Test
    fun `resend verification for unknown email returns 204 silently`() {
        mockMvc.perform(post("/auth/resend-verification").param("email", "unknown@test.com"))
            .andExpect(status().isNoContent)

        verifyNoInteractions(emailService)
    }

    @Test
    fun `resend verification for already activated artist returns 204 silently`() {
        val artistId = UUID.randomUUID()
        artistRepository.save(TattooArtist(id = artistId, email = "artist@test.com", password = "hash", registeredAt = Instant.now(), activatedAt = Instant.now()))

        mockMvc.perform(post("/auth/resend-verification").param("email", "artist@test.com"))
            .andExpect(status().isNoContent)

        verifyNoInteractions(emailService)
    }

    @Test
    fun `login with valid credentials returns access and refresh tokens`() {
        val artistId = UUID.randomUUID()
        artistRepository.save(
            TattooArtist(
                id = artistId,
                email = "artist@test.com",
                password = passwordEncoder.encode("password123")!!,
                registeredAt = Instant.now(),
                activatedAt = Instant.now(),
            )
        )

        mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(LoginRequestDto("artist@test.com", "password123")))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").isString)
            .andExpect(jsonPath("$.refreshToken").isString)

        assertThat(refreshTokenRepository.findAll()).hasSize(1)
    }

    @Test
    fun `login with wrong password returns 401`() {
        val artistId = UUID.randomUUID()
        artistRepository.save(
            TattooArtist(
                id = artistId,
                email = "artist@test.com",
                password = passwordEncoder.encode("password123")!!,
                registeredAt = Instant.now(),
                activatedAt = Instant.now(),
            )
        )

        mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(LoginRequestDto("artist@test.com", "wrongpassword")))
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.error").value("Invalid email or password"))
    }

    @Test
    fun `login with unknown email returns 401`() {
        mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(LoginRequestDto("unknown@test.com", "password123")))
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.error").value("Invalid email or password"))
    }

    @Test
    fun `refresh with valid token returns new access and refresh tokens and deletes old token`() {
        val artistId = UUID.randomUUID()
        artistRepository.save(
            TattooArtist(
                id = artistId,
                email = "artist@test.com",
                password = "hash",
                registeredAt = Instant.now(),
                activatedAt = Instant.now(),
            )
        )
        val oldToken = RefreshToken(tattooArtistId = artistId)
        refreshTokenRepository.save(oldToken)

        val result = mockMvc.perform(
            post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(RefreshRequestDto(oldToken.token.toString())))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").isString)
            .andExpect(jsonPath("$.refreshToken").isString)
            .andReturn()

        val response = objectMapper.readValue(result.response.contentAsString, Map::class.java)
        assertThat(response["refreshToken"]).isNotEqualTo(oldToken.token.toString())
        assertThat(refreshTokenRepository.findById(oldToken.token)).isEmpty()
        assertThat(refreshTokenRepository.findAll()).hasSize(1)
    }

    @Test
    fun `refresh with expired token returns 401 and deletes the token`() {
        val artistId = UUID.randomUUID()
        artistRepository.save(
            TattooArtist(
                id = artistId,
                email = "artist@test.com",
                password = "hash",
                registeredAt = Instant.now(),
                activatedAt = Instant.now(),
            )
        )
        val expiredToken = RefreshToken(
            tattooArtistId = artistId,
            expiresAt = Instant.now().minusSeconds(1),
        )
        refreshTokenRepository.save(expiredToken)

        mockMvc.perform(
            post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(RefreshRequestDto(expiredToken.token.toString())))
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.error").value("Refresh token is invalid or expired"))

        assertThat(refreshTokenRepository.findAll()).isEmpty()
    }

    @Test
    fun `refresh with unknown token returns 401`() {
        mockMvc.perform(
            post("/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(RefreshRequestDto(UUID.randomUUID().toString())))
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.error").value("Refresh token is invalid or expired"))
    }

    @Test
    fun `logout with valid token returns 204 and deletes token`() {
        val artistId = UUID.randomUUID()
        artistRepository.save(
            TattooArtist(
                id = artistId,
                email = "artist@test.com",
                password = "hash",
                registeredAt = Instant.now(),
                activatedAt = Instant.now(),
            )
        )
        val token = RefreshToken(tattooArtistId = artistId)
        refreshTokenRepository.save(token)

        mockMvc.perform(
            post("/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(LogoutRequestDto(token.token.toString())))
        )
            .andExpect(status().isNoContent)

        assertThat(refreshTokenRepository.findAll()).isEmpty()
    }

    @Test
    fun `logout with unknown token returns 204 silently`() {
        mockMvc.perform(
            post("/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(LogoutRequestDto(UUID.randomUUID().toString())))
        )
            .andExpect(status().isNoContent)
    }

    @Test
    fun `protected endpoint without token returns 401 with error message`() {
        mockMvc.perform(get("/actuator/health"))
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.error").value("Unauthorized"))
    }

    @Test
    fun `protected endpoint with invalid token returns 401 with error message`() {
        mockMvc.perform(
            get("/actuator/health")
                .header("Authorization", "Bearer this.is.invalid")
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.error").value("Unauthorized"))
    }

    @Test
    fun `protected endpoint with valid access token returns 200`() {
        val token = jwtService.generateAccessToken(UUID.randomUUID())
        mockMvc.perform(
            get("/actuator/health")
                .header("Authorization", "Bearer $token")
        )
            .andExpect(status().isOk)
    }

    @Test
    fun `login with unactivated account returns 403`() {
        val artistId = UUID.randomUUID()
        artistRepository.save(
            TattooArtist(
                id = artistId,
                email = "artist@test.com",
                password = passwordEncoder.encode("password123")!!,
                registeredAt = Instant.now(),
                activatedAt = null,
            )
        )

        mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(LoginRequestDto("artist@test.com", "password123")))
        )
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.error").value("Account is not activated"))
    }
}
