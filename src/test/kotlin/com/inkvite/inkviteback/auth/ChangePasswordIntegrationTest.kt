package com.inkvite.inkviteback.auth

import com.inkvite.inkviteback.AbstractIntegrationTest
import com.inkvite.inkviteback.artist.entity.TattooArtist
import com.inkvite.inkviteback.artist.repository.TattooArtistRepository
import com.inkvite.inkviteback.auth.dto.ChangePasswordRequestDto
import com.inkvite.inkviteback.auth.dto.LoginRequestDto
import com.inkvite.inkviteback.auth.entity.RefreshToken
import com.inkvite.inkviteback.auth.repository.RefreshTokenRepository
import com.inkvite.inkviteback.auth.service.JwtService
import com.inkvite.inkviteback.email.service.EmailService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.ObjectMapper
import java.time.Instant
import java.util.UUID

class ChangePasswordIntegrationTest : AbstractIntegrationTest() {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var objectMapper: ObjectMapper
    @Autowired lateinit var artistRepository: TattooArtistRepository
    @Autowired lateinit var refreshTokenRepository: RefreshTokenRepository
    @Autowired lateinit var passwordEncoder: PasswordEncoder
    @Autowired lateinit var jwtService: JwtService

    @MockitoBean
    lateinit var emailService: EmailService

    private lateinit var artistId: UUID

    @BeforeEach
    fun setup() {
        refreshTokenRepository.deleteAll()
        artistRepository.deleteAll()
        artistId = UUID.randomUUID()
        artistRepository.save(
            TattooArtist(
                id = artistId,
                email = "artist@test.com",
                password = passwordEncoder.encode("oldPassword1")!!,
                artistName = "Test Artist",
                slug = "test-artist",
                city = "Test City",
                countryCode = "FR",
                registeredAt = Instant.now(),
                activatedAt = Instant.now(),
            )
        )
    }

    private fun accessToken() = jwtService.generateAccessToken(artistId)

    @Test
    fun `change password with correct current password updates password, wipes refresh tokens, and returns new tokens`() {
        // seed two existing refresh tokens to verify they are wiped
        refreshTokenRepository.save(RefreshToken(tattooArtistId = artistId))
        refreshTokenRepository.save(RefreshToken(tattooArtistId = artistId))

        mockMvc.perform(
            post("/auth/change-password")
                .header("Authorization", "Bearer ${accessToken()}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ChangePasswordRequestDto("oldPassword1", "newPassword1")))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").isString)
            .andExpect(jsonPath("$.refreshToken").isString)

        // old refresh tokens wiped; only the new one from login() remains
        assertThat(refreshTokenRepository.findAll()).hasSize(1)

        // password actually changed — can log in with new password
        mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(LoginRequestDto("artist@test.com", "newPassword1")))
        ).andExpect(status().isOk)

        // security notification email sent
        verify(emailService).sendPasswordChangedEmail("artist@test.com", "Test Artist")
    }

    @Test
    fun `change password with wrong current password returns 401`() {
        mockMvc.perform(
            post("/auth/change-password")
                .header("Authorization", "Bearer ${accessToken()}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ChangePasswordRequestDto("wrongPassword", "newPassword1")))
        )
            .andExpect(status().isUnauthorized)
            .andExpect(jsonPath("$.error").value("Invalid email or password"))
    }

    @Test
    fun `change password with too short new password returns 400`() {
        mockMvc.perform(
            post("/auth/change-password")
                .header("Authorization", "Bearer ${accessToken()}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ChangePasswordRequestDto("oldPassword1", "short")))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `change password without authentication returns 401`() {
        mockMvc.perform(
            post("/auth/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ChangePasswordRequestDto("oldPassword1", "newPassword1")))
        )
            .andExpect(status().isUnauthorized)
    }
}
