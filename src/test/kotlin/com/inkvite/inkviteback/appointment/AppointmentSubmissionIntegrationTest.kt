package com.inkvite.inkviteback.appointment

import com.inkvite.inkviteback.email.service.EmailService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.mockito.kotlin.argThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.ObjectMapper
import java.util.*

class AppointmentSubmissionIntegrationTest : AbstractAppointmentIntegrationTest() {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var objectMapper: ObjectMapper

    @MockitoBean lateinit var emailService: EmailService

    private fun validFormBody(coverUp: Boolean = false, references: List<Map<String, Any?>> = emptyList()) =
        mapOf(
            "email" to "client@test.com",
            "firstName" to "Jane",
            "lastName" to "Doe",
            "description" to "A beautiful dragon tattoo on my forearm",
            "placement" to "forearm",
            "size" to "10x10cm",
            "firstTattoo" to false,
            "coverUp" to coverUp,
            "references" to references
        )

    // --- POST /appointment/{slug} ---

    @Test
    fun `submit appointment form saves form and client and sends verification email`() {
        val artist = createActivatedArtist()

        mockMvc.perform(
            post("/appointment/${artist.slug}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validFormBody()))
        ).andExpect(status().isCreated)

        val form = appointmentRepository.findAll().single()
        assertThat(form.artist.id).isEqualTo(artist.id)
        assertThat(form.tattooDescription).isEqualTo("A beautiful dragon tattoo on my forearm")
        assertThat(form.tattooPlacement).isEqualTo("forearm")
        assertThat(form.tattooSize).isEqualTo("10x10cm")
        assertThat(form.firstTattoo).isFalse()
        assertThat(form.coverUp).isFalse()
        assertThat(form.submittedAt).isNotNull()
        assertThat(form.verifiedAt).isNull()

        val client = tattooClientRepository.findAll().single()
        assertThat(client.email).isEqualTo("client@test.com")
        assertThat(client.firstName).isEqualTo("Jane")
        assertThat(client.lastName).isEqualTo("Doe")

        verify(emailService).sendAppointmentVerificationEmail(argThat { id == form.id })
    }

    @Test
    fun `submit appointment form with references saves references`() {
        val artist = createActivatedArtist()
        val crop = mapOf("left" to 232, "top" to 309, "width" to 1853, "height" to 2470)
        val refs = listOf(
            mapOf("key" to "uploads/ref1.jpg", "comment" to "Like this style", "crop" to crop),
            mapOf("key" to "uploads/ref2.jpg", "comment" to null, "crop" to crop)
        )

        mockMvc.perform(
            post("/appointment/${artist.slug}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validFormBody(coverUp = true, references = refs)))
        ).andExpect(status().isCreated)

        val savedRefs = referenceRepository.findAll()
        assertThat(savedRefs).hasSize(2)
        assertThat(savedRefs.map { it.key }).containsExactlyInAnyOrder("uploads/ref1.jpg", "uploads/ref2.jpg")
        assertThat(savedRefs.first { it.key == "uploads/ref1.jpg" }.comment).isEqualTo("Like this style")
        assertThat(savedRefs.first { it.key == "uploads/ref2.jpg" }.comment).isNull()
        savedRefs.forEach { ref ->
            assertThat(ref.cropLeft).isEqualTo(232)
            assertThat(ref.cropTop).isEqualTo(309)
            assertThat(ref.cropWidth).isEqualTo(1853)
            assertThat(ref.cropHeight).isEqualTo(2470)
        }
    }

    @Test
    fun `submit from same client email twice reuses existing tattoo client`() {
        val artist = createActivatedArtist()

        mockMvc.perform(
            post("/appointment/${artist.slug}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validFormBody()))
        ).andExpect(status().isCreated)

        mockMvc.perform(
            post("/appointment/${artist.slug}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validFormBody()))
        ).andExpect(status().isCreated)

        assertThat(tattooClientRepository.findAll()).hasSize(1)
        assertThat(appointmentRepository.findAll()).hasSize(2)
    }

    @Test
    fun `submit appointment form for unknown slug returns 404`() {
        mockMvc.perform(
            post("/appointment/unknown-slug")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validFormBody()))
        ).andExpect(status().isNotFound)
    }

    @Test
    fun `submit appointment form with invalid slug format returns 400`() {
        mockMvc.perform(
            post("/appointment/INVALID")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validFormBody()))
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `submit appointment form with invalid email returns 400`() {
        createActivatedArtist()

        mockMvc.perform(
            post("/appointment/test-artist")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validFormBody().toMutableMap().apply { put("email", "not-an-email") }))
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `submit cover appointment form without references returns 400`() {
        createActivatedArtist()

        mockMvc.perform(
            post("/appointment/test-artist")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validFormBody(coverUp = true)))
        ).andExpect(status().isBadRequest)
    }

    // --- POST /appointment/{slug}/reference ---

    @Test
    fun `upload reference with jpeg returns 201 with key and signed url`() {
        val artist = createActivatedArtist()

        mockMvc.perform(
            multipart("/appointment/${artist.slug}/reference")
                .file(MockMultipartFile("image", "ref.jpg", "image/jpeg", ByteArray(100)))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.key").value(org.hamcrest.Matchers.matchesPattern("references/${artist.id}/[0-9a-f\\-]{36}")))
            .andExpect(jsonPath("$.url").value(org.hamcrest.Matchers.containsString("X-Amz-Signature")))
    }

    @Test
    fun `upload reference with png returns 201`() {
        val artist = createActivatedArtist()

        mockMvc.perform(
            multipart("/appointment/${artist.slug}/reference")
                .file(MockMultipartFile("image", "ref.png", "image/png", ByteArray(100)))
        ).andExpect(status().isCreated)
    }

    @Test
    fun `upload reference with webp returns 201`() {
        val artist = createActivatedArtist()

        mockMvc.perform(
            multipart("/appointment/${artist.slug}/reference")
                .file(MockMultipartFile("image", "ref.webp", "image/webp", ByteArray(100)))
        ).andExpect(status().isCreated)
    }

    @Test
    fun `upload reference with invalid content type returns 400`() {
        val artist = createActivatedArtist()

        mockMvc.perform(
            multipart("/appointment/${artist.slug}/reference")
                .file(MockMultipartFile("image", "file.pdf", "application/pdf", ByteArray(100)))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Image must be a JPEG, PNG, or WebP image"))
    }

    @Test
    fun `upload reference exceeding 5mb returns 400`() {
        val artist = createActivatedArtist()

        mockMvc.perform(
            multipart("/appointment/${artist.slug}/reference")
                .file(MockMultipartFile("image", "big.jpg", "image/jpeg", ByteArray(5 * 1024 * 1024 + 1)))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Image must not exceed 5 MB"))
    }

    @Test
    fun `upload reference for unknown slug returns 404`() {
        mockMvc.perform(
            multipart("/appointment/unknown-slug/reference")
                .file(MockMultipartFile("image", "ref.jpg", "image/jpeg", ByteArray(100)))
        ).andExpect(status().isNotFound)
    }

    @Test
    fun `upload reference with invalid slug format returns 400`() {
        mockMvc.perform(
            multipart("/appointment/INVALID/reference")
                .file(MockMultipartFile("image", "ref.jpg", "image/jpeg", ByteArray(100)))
        ).andExpect(status().isBadRequest)
    }

    // --- GET /appointment/verify ---

    @Test
    fun `verify appointment form sets verifiedAt and returns 204`() {
        val artist = createActivatedArtist()
        mockMvc.perform(
            post("/appointment/${artist.slug}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validFormBody()))
        )
        val form = appointmentRepository.findAll().single()
        assertThat(form.verifiedAt).isNull()

        mockMvc.perform(get("/appointment/verify").param("appointmentId", form.id.toString()))
            .andExpect(status().isNoContent)

        assertThat(appointmentRepository.findById(form.id).get().verifiedAt).isNotNull()
    }

    @Test
    fun `verify appointment form sends notification email to artist`() {
        val artist = createActivatedArtist()
        mockMvc.perform(
            post("/appointment/${artist.slug}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validFormBody()))
        )
        val form = appointmentRepository.findAll().single()

        mockMvc.perform(get("/appointment/verify").param("appointmentId", form.id.toString()))
            .andExpect(status().isNoContent)

        verify(emailService).sendAppointmentNotificationEmail(argThat { id == form.id })
    }

    @Test
    fun `verify appointment form with unknown id returns 404`() {
        mockMvc.perform(get("/appointment/verify").param("appointmentId", UUID.randomUUID().toString()))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.error").value("Appointment not found"))
    }
}
