package com.inkvite.inkviteback.support

import com.inkvite.inkviteback.AbstractIntegrationTest
import com.inkvite.inkviteback.artist.entity.TattooArtist
import com.inkvite.inkviteback.artist.repository.TattooArtistRepository
import com.inkvite.inkviteback.auth.service.JwtService
import com.inkvite.inkviteback.support.entity.SupportMessageType
import com.inkvite.inkviteback.support.repository.SupportMessageRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.ObjectMapper
import java.time.Instant
import java.util.*

class SupportMessageIntegrationTest : AbstractIntegrationTest() {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var objectMapper: ObjectMapper
    @Autowired lateinit var jwtService: JwtService
    @Autowired lateinit var artistRepository: TattooArtistRepository
    @Autowired lateinit var supportMessageRepository: SupportMessageRepository

    @AfterEach
    fun cleanup() {
        supportMessageRepository.deleteAll()
        artistRepository.deleteAll()
    }

    private fun createActivatedArtist(): TattooArtist =
        artistRepository.save(
            TattooArtist(
                id = UUID.randomUUID(),
                email = "artist@test.com",
                password = "hashed",
                artistName = "Test Artist",
                slug = "test-artist",
                city = "Test City",
                countryCode = "FR",
                registeredAt = Instant.now(),
                activatedAt = Instant.now(),
            )
        )

    private fun messageBody(
        type: String = "BUG",
        message: String = "Something is broken",
        screenshot: String? = null
    ) = mapOf("type" to type, "message" to message, "screenshot" to screenshot)

    // --- POST /support/screenshot ---

    @Test
    fun `upload screenshot returns 200 with key and signed url`() {
        val artist = createActivatedArtist()
        val token = jwtService.generateAccessToken(artist.id)

        mockMvc.perform(
            multipart("/support/screenshot")
                .file(MockMultipartFile("image", "shot.png", "image/png", ByteArray(100)))
                .header("Authorization", "Bearer $token")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.key").value(org.hamcrest.Matchers.matchesPattern("contact-screenshots/${artist.id}/[0-9a-f\\-]{36}")))
            .andExpect(jsonPath("$.url").value(org.hamcrest.Matchers.containsString("X-Amz-Signature")))
    }

    @Test
    fun `upload screenshot with invalid content type returns 400`() {
        val artist = createActivatedArtist()
        val token = jwtService.generateAccessToken(artist.id)

        mockMvc.perform(
            multipart("/support/screenshot")
                .file(MockMultipartFile("image", "file.pdf", "application/pdf", ByteArray(100)))
                .header("Authorization", "Bearer $token")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Image must be a JPEG, PNG, or WebP image"))
    }

    @Test
    fun `upload screenshot returns 401 when unauthenticated`() {
        mockMvc.perform(
            multipart("/support/screenshot")
                .file(MockMultipartFile("image", "shot.png", "image/png", ByteArray(100)))
        ).andExpect(status().isUnauthorized)
    }

    // --- POST /support ---

    @Test
    fun `submit support message without screenshot returns 204 and persists`() {
        val artist = createActivatedArtist()
        val token = jwtService.generateAccessToken(artist.id)

        mockMvc.perform(
            post("/support")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer $token")
                .content(objectMapper.writeValueAsString(messageBody(type = "HELP", message = "How do I change my slug?")))
        ).andExpect(status().isNoContent)

        val saved = supportMessageRepository.findAll().single()
        assertThat(saved.artist.id).isEqualTo(artist.id)
        assertThat(saved.type).isEqualTo(SupportMessageType.HELP)
        assertThat(saved.message).isEqualTo("How do I change my slug?")
        assertThat(saved.screenshotKey).isNull()
        assertThat(saved.createdAt).isNotNull()
    }

    @Test
    fun `submit support message with screenshot persists key`() {
        val artist = createActivatedArtist()
        val token = jwtService.generateAccessToken(artist.id)
        val screenshot = "contact-screenshots/${artist.id}/abc"

        mockMvc.perform(
            post("/support")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer $token")
                .content(objectMapper.writeValueAsString(messageBody(type = "IDEA", message = "Add dark mode", screenshot = screenshot)))
        ).andExpect(status().isNoContent)

        val saved = supportMessageRepository.findAll().single()
        assertThat(saved.screenshotKey).isEqualTo("contact-screenshots/${artist.id}/abc")
    }

    @Test
    fun `submit support message with blank message returns 400`() {
        val artist = createActivatedArtist()
        val token = jwtService.generateAccessToken(artist.id)

        mockMvc.perform(
            post("/support")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer $token")
                .content(objectMapper.writeValueAsString(messageBody(message = "")))
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `submit support message exceeding 1500 characters returns 400`() {
        val artist = createActivatedArtist()
        val token = jwtService.generateAccessToken(artist.id)

        mockMvc.perform(
            post("/support")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer $token")
                .content(objectMapper.writeValueAsString(messageBody(message = "a".repeat(1501))))
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `submit support message with screenshot exceeding 1024 characters returns 400`() {
        val artist = createActivatedArtist()
        val token = jwtService.generateAccessToken(artist.id)

        mockMvc.perform(
            post("/support")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer $token")
                .content(objectMapper.writeValueAsString(messageBody(screenshot = "a".repeat(1025))))
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `submit support message returns 401 when unauthenticated`() {
        mockMvc.perform(
            post("/support")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(messageBody()))
        ).andExpect(status().isUnauthorized)
    }
}
