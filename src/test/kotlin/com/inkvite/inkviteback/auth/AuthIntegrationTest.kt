package com.inkvite.inkviteback.auth

import com.inkvite.inkviteback.TestcontainersConfiguration
import com.inkvite.inkviteback.artist.TattooArtist
import com.inkvite.inkviteback.artist.TattooArtistRepository
import com.inkvite.inkviteback.auth.dto.RegisterRequestDto
import com.inkvite.inkviteback.auth.entity.VerificationToken
import com.inkvite.inkviteback.auth.repository.VerificationTokenRepository
import com.inkvite.inkviteback.email.service.EmailService
import org.assertj.core.api.Assertions.assertThat
import java.time.Instant
import java.util.UUID
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
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

    @MockitoBean lateinit var emailService: EmailService

    @BeforeEach
    fun cleanup() {
        tokenRepository.deleteAll()
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
        assertThat(artist.active).isFalse()

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

        assertThat(artistRepository.findAll().single().active).isTrue()
        assertThat(tokenRepository.findAll()).isEmpty()
    }

    @Test
    fun `verify with expired token returns 400 and deletes token`() {
        val artistId = UUID.randomUUID()
        artistRepository.save(TattooArtist(id = artistId, email = "artist@test.com", passwordHash = "hash"))
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
}
