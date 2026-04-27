package com.inkvite.inkviteback.auth

import com.inkvite.inkviteback.AbstractIntegrationTest
import com.inkvite.inkviteback.artist.entity.TattooArtist
import com.inkvite.inkviteback.artist.repository.TattooArtistRepository
import com.inkvite.inkviteback.auth.dto.LoginRequestDto
import com.inkvite.inkviteback.auth.dto.LogoutRequestDto
import com.inkvite.inkviteback.auth.dto.RefreshRequestDto
import com.inkvite.inkviteback.auth.dto.RegisterRequestDto
import com.inkvite.inkviteback.auth.dto.ResetPasswordRequestDto
import com.inkvite.inkviteback.auth.entity.PasswordResetToken
import com.inkvite.inkviteback.auth.entity.RefreshToken
import com.inkvite.inkviteback.auth.entity.VerificationToken
import com.inkvite.inkviteback.auth.repository.PasswordResetTokenRepository
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
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.ObjectMapper

class AuthIntegrationTest : AbstractIntegrationTest() {

    @Autowired
    lateinit var mockMvc: MockMvc
    @Autowired
    lateinit var objectMapper: ObjectMapper
    @Autowired
    lateinit var tokenRepository: VerificationTokenRepository
    @Autowired
    lateinit var passwordResetTokenRepository: PasswordResetTokenRepository
    @Autowired
    lateinit var artistRepository: TattooArtistRepository
    @Autowired
    lateinit var refreshTokenRepository: RefreshTokenRepository
    @Autowired
    lateinit var passwordEncoder: PasswordEncoder
    @Autowired
    lateinit var jwtService: JwtService

    @MockitoBean
    lateinit var emailService: EmailService

    @BeforeEach
    fun cleanup() {
        passwordResetTokenRepository.deleteAll()
        tokenRepository.deleteAll()
        refreshTokenRepository.deleteAll()
        artistRepository.deleteAll()
    }

    @Test
    fun `register creates inactive artist and sends verification email`() {
        mockMvc.perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        RegisterRequestDto(
                            "artist@test.com",
                            "password123",
                            "John Doe",
                            "john-doe"
                        )
                    )
                )
        ).andExpect(status().isNoContent)

        val artist = artistRepository.findAll().single()
        assertThat(artist.email).isEqualTo("artist@test.com")
        assertThat(artist.registeredAt).isNotNull()
        assertThat(artist.activatedAt).isNull()
        assertThat(artist.artistName).isEqualTo("John Doe")
        assertThat(artist.slug).isEqualTo("john-doe")

        val token = tokenRepository.findAll().single()
        verify(emailService).sendArtistVerificationEmail("artist@test.com", "John Doe", token.token)
    }

    @Test
    fun `register with email of verified account returns 409`() {
        val artistId = UUID.randomUUID()
        artistRepository.save(
            TattooArtist(
                id = artistId,
                email = "artist@test.com",
                password = "hash",
                artistName = "John Doe",
                slug = "john-doe",
                registeredAt = Instant.now(),
                activatedAt = Instant.now()
            )
        )

        val body = objectMapper.writeValueAsString(
            RegisterRequestDto(
                "artist@test.com",
                "password123",
                "John Doe 2",
                "john-doe-2"
            )
        )
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isConflict)
    }

    @Test
    fun `register with email of unverified account replaces it and sends new verification email`() {
        mockMvc.perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        RegisterRequestDto(
                            "artist@test.com",
                            "password123",
                            "John Doe",
                            "john-doe"
                        )
                    )
                )
        ).andExpect(status().isNoContent)
        val firstArtistId = artistRepository.findAll().single().id
        val firstToken = tokenRepository.findAll().single().token

        mockMvc.perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        RegisterRequestDto(
                            "artist@test.com",
                            "newpassword123",
                            "Jane Doe",
                            "jane-doe"
                        )
                    )
                )
        ).andExpect(status().isNoContent)

        val artists = artistRepository.findAll()
        assertThat(artists).hasSize(1)
        val newArtist = artists.single()
        assertThat(newArtist.id).isNotEqualTo(firstArtistId)
        assertThat(newArtist.artistName).isEqualTo("Jane Doe")
        assertThat(newArtist.slug).isEqualTo("jane-doe")
        assertThat(newArtist.activatedAt).isNull()

        val tokens = tokenRepository.findAll()
        assertThat(tokens).hasSize(1)
        assertThat(tokens.single().token).isNotEqualTo(firstToken)
        verify(emailService).sendArtistVerificationEmail("artist@test.com", "Jane Doe", tokens.single().token)
    }

    @Test
    fun `register with email of unverified account frees old slug allowing re-use`() {
        mockMvc.perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        RegisterRequestDto(
                            "artist@test.com",
                            "password123",
                            "John Doe",
                            "john-doe"
                        )
                    )
                )
        ).andExpect(status().isNoContent)

        mockMvc.perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        RegisterRequestDto(
                            "artist@test.com",
                            "newpassword123",
                            "John Doe",
                            "john-doe"
                        )
                    )
                )
        ).andExpect(status().isNoContent)

        assertThat(artistRepository.findAll()).hasSize(1)
        assertThat(tokenRepository.findAll()).hasSize(1)
    }

    @Test
    fun `verify with valid token activates artist, deletes token, and returns tokens`() {
        mockMvc.perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        RegisterRequestDto(
                            "artist@test.com",
                            "password123",
                            "John Doe",
                            "john-doe"
                        )
                    )
                )
        )
        val token = tokenRepository.findAll().single().token

        mockMvc.perform(get("/auth/verify").param("token", token))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").isString)
            .andExpect(jsonPath("$.refreshToken").isString)

        assertThat(artistRepository.findAll().single().activatedAt).isNotNull()
        assertThat(tokenRepository.findAll()).isEmpty()
        assertThat(refreshTokenRepository.findAll()).hasSize(1)
    }

    @Test
    fun `verify with expired token returns 400 and deletes token`() {
        val artistId = UUID.randomUUID()
        artistRepository.save(
            TattooArtist(
                id = artistId,
                email = "artist@test.com",
                password = "hash",
                artistName = "Test Artist",
                slug = "test-artist",
                registeredAt = Instant.now()
            )
        )
        tokenRepository.save(
            VerificationToken(
                token = "expired-token",
                tattooArtistId = artistId,
                expiresAt = Instant.now().minusSeconds(1)
            )
        )

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
        val body =
            objectMapper.writeValueAsString(RegisterRequestDto("not-an-email", "password123", "John Doe", "john-doe"))
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `register with too short password returns 400`() {
        val body =
            objectMapper.writeValueAsString(RegisterRequestDto("artist@test.com", "short", "John Doe", "john-doe"))
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `resend verification replaces existing token and sends new email`() {
        mockMvc.perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        RegisterRequestDto(
                            "artist@test.com",
                            "password123",
                            "John Doe",
                            "john-doe"
                        )
                    )
                )
        )
        val firstToken = tokenRepository.findAll().single().token

        mockMvc.perform(post("/auth/resend-verification").param("email", "artist@test.com"))
            .andExpect(status().isNoContent)

        val newToken = tokenRepository.findAll().single().token
        assertThat(newToken).isNotEqualTo(firstToken)
        verify(emailService).sendArtistVerificationEmail("artist@test.com", "John Doe", newToken)
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
        artistRepository.save(
            TattooArtist(
                id = artistId,
                email = "artist@test.com",
                password = "hash",
                artistName = "Test Artist",
                slug = "test-artist",
                registeredAt = Instant.now(),
                activatedAt = Instant.now()
            )
        )

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
                artistName = "Test Artist",
                slug = "test-artist",
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
                artistName = "Test Artist",
                slug = "test-artist",
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
                artistName = "Test Artist",
                slug = "test-artist",
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
                artistName = "Test Artist",
                slug = "test-artist",
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
                artistName = "Test Artist",
                slug = "test-artist",
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
                artistName = "Test Artist",
                slug = "test-artist",
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

    @Test
    fun `register with duplicate slug returns 409`() {
        val body1 = objectMapper.writeValueAsString(
            RegisterRequestDto(
                "artist1@test.com",
                "password123",
                "John Doe",
                "john-doe"
            )
        )
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(body1))
            .andExpect(status().isNoContent)

        val body2 = objectMapper.writeValueAsString(
            RegisterRequestDto(
                "artist2@test.com",
                "password123",
                "Jane Doe",
                "john-doe"
            )
        )
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(body2))
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.error").value("This slug is already taken"))
    }

    @Test
    fun `register with invalid slug format returns 400`() {
        val body = objectMapper.writeValueAsString(
            RegisterRequestDto(
                "artist@test.com",
                "password123",
                "John Doe",
                "INVALID SLUG!"
            )
        )
        mockMvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest)
    }

    // --- forgot-password ---

    @Test
    fun `forgot password with valid activated email sends reset email and saves token`() {
        val artistId = UUID.randomUUID()
        artistRepository.save(
            TattooArtist(
                id = artistId,
                email = "artist@test.com",
                password = "hash",
                artistName = "Test Artist",
                slug = "test-artist",
                registeredAt = Instant.now(),
                activatedAt = Instant.now()
            )
        )

        mockMvc.perform(post("/auth/forgot-password").param("email", "artist@test.com"))
            .andExpect(status().isNoContent)

        val token = passwordResetTokenRepository.findAll().single()
        assertThat(token.tattooArtistId).isEqualTo(artistId)
        verify(emailService).sendPasswordResetEmail("artist@test.com", "Test Artist", token.token)
    }

    @Test
    fun `forgot password with unknown email returns 204 silently`() {
        mockMvc.perform(post("/auth/forgot-password").param("email", "unknown@test.com"))
            .andExpect(status().isNoContent)

        assertThat(passwordResetTokenRepository.findAll()).isEmpty()
        verifyNoInteractions(emailService)
    }

    @Test
    fun `forgot password with unactivated account returns 204 silently`() {
        val artistId = UUID.randomUUID()
        artistRepository.save(
            TattooArtist(
                id = artistId,
                email = "artist@test.com",
                password = "hash",
                artistName = "Test Artist",
                slug = "test-artist",
                registeredAt = Instant.now(),
                activatedAt = null
            )
        )

        mockMvc.perform(post("/auth/forgot-password").param("email", "artist@test.com"))
            .andExpect(status().isNoContent)

        assertThat(passwordResetTokenRepository.findAll()).isEmpty()
        verifyNoInteractions(emailService)
    }

    @Test
    fun `forgot password replaces existing token when requested again`() {
        val artistId = UUID.randomUUID()
        artistRepository.save(
            TattooArtist(
                id = artistId,
                email = "artist@test.com",
                password = "hash",
                artistName = "Test Artist",
                slug = "test-artist",
                registeredAt = Instant.now(),
                activatedAt = Instant.now()
            )
        )
        val oldToken = PasswordResetToken(tattooArtistId = artistId)
        passwordResetTokenRepository.save(oldToken)

        mockMvc.perform(post("/auth/forgot-password").param("email", "artist@test.com"))
            .andExpect(status().isNoContent)

        val tokens = passwordResetTokenRepository.findAll()
        assertThat(tokens).hasSize(1)
        assertThat(tokens.single().token).isNotEqualTo(oldToken.token)
    }

    // --- reset-password ---

    @Test
    fun `reset password with valid token updates password, wipes refresh tokens, and returns new tokens`() {
        val artistId = UUID.randomUUID()
        artistRepository.save(
            TattooArtist(
                id = artistId,
                email = "artist@test.com",
                password = passwordEncoder.encode("oldPassword1")!!,
                artistName = "Test Artist",
                slug = "test-artist",
                registeredAt = Instant.now(),
                activatedAt = Instant.now()
            )
        )
        refreshTokenRepository.save(RefreshToken(tattooArtistId = artistId))
        refreshTokenRepository.save(RefreshToken(tattooArtistId = artistId))
        val resetToken = PasswordResetToken(tattooArtistId = artistId)
        passwordResetTokenRepository.save(resetToken)

        mockMvc.perform(
            post("/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ResetPasswordRequestDto(resetToken.token, "newPassword1")))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").isString)
            .andExpect(jsonPath("$.refreshToken").isString)

        assertThat(passwordResetTokenRepository.findAll()).isEmpty()
        // old refresh tokens wiped; only the new one from auto-login remains
        assertThat(refreshTokenRepository.findAll()).hasSize(1)
        // can log in with new password
        mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(LoginRequestDto("artist@test.com", "newPassword1")))
        ).andExpect(status().isOk)
    }

    @Test
    fun `reset password with unknown token returns 404`() {
        mockMvc.perform(
            post("/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ResetPasswordRequestDto("unknown-token", "newPassword1")))
        ).andExpect(status().isNotFound)
    }

    @Test
    fun `reset password with expired token returns 400 and deletes token`() {
        val artistId = UUID.randomUUID()
        artistRepository.save(
            TattooArtist(
                id = artistId,
                email = "artist@test.com",
                password = "hash",
                artistName = "Test Artist",
                slug = "test-artist",
                registeredAt = Instant.now(),
                activatedAt = Instant.now()
            )
        )
        val expiredToken = PasswordResetToken(tattooArtistId = artistId, expiresAt = Instant.now().minusSeconds(1))
        passwordResetTokenRepository.save(expiredToken)

        mockMvc.perform(
            post("/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ResetPasswordRequestDto(expiredToken.token, "newPassword1")))
        ).andExpect(status().isBadRequest)

        assertThat(passwordResetTokenRepository.findAll()).isEmpty()
    }

    @Test
    fun `reset password with too short password returns 400`() {
        mockMvc.perform(
            post("/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ResetPasswordRequestDto("some-token", "short")))
        ).andExpect(status().isBadRequest)
    }
}
