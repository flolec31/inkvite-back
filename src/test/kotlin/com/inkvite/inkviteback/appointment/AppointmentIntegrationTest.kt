package com.inkvite.inkviteback.appointment

import com.inkvite.inkviteback.AbstractIntegrationTest
import com.inkvite.inkviteback.appointment.repository.AppointmentFormRepository
import com.inkvite.inkviteback.appointment.repository.ReferenceRepository
import com.inkvite.inkviteback.client.repository.TattooClientRepository
import com.inkvite.inkviteback.artist.entity.TattooArtist
import com.inkvite.inkviteback.artist.repository.TattooArtistRepository
import com.inkvite.inkviteback.email.service.EmailService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.ObjectMapper
import java.time.Instant
import java.util.UUID

class AppointmentIntegrationTest : AbstractIntegrationTest() {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var objectMapper: ObjectMapper
    @Autowired lateinit var artistRepository: TattooArtistRepository
    @Autowired lateinit var appointmentFormRepository: AppointmentFormRepository
    @Autowired lateinit var tattooClientRepository: TattooClientRepository
    @Autowired lateinit var referenceRepository: ReferenceRepository

    @MockitoBean lateinit var emailService: EmailService

    @BeforeEach
    @AfterEach
    fun cleanup() {
        referenceRepository.deleteAll()
        appointmentFormRepository.deleteAll()
        tattooClientRepository.deleteAll()
        artistRepository.deleteAll()
    }

    private fun createActivatedArtist(slug: String = "test-artist"): TattooArtist =
        artistRepository.save(
            TattooArtist(
                id = UUID.randomUUID(),
                email = "$slug@test.com",
                password = "hashed",
                artistName = "Test Artist",
                slug = slug,
                registeredAt = Instant.now(),
                activatedAt = Instant.now(),
            )
        )

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

        val form = appointmentFormRepository.findAll().single()
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

        verify(emailService).sendAppointmentVerificationEmail("client@test.com", form.id)
    }

    @Test
    fun `submit appointment form with references saves references`() {
        val artist = createActivatedArtist()
        val refs = listOf(
            mapOf("key" to "uploads/ref1.jpg", "comment" to "Like this style"),
            mapOf("key" to "uploads/ref2.jpg", "comment" to null)
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
        assertThat(appointmentFormRepository.findAll()).hasSize(2)
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
    fun `submit appointment form with description too short returns 400`() {
        createActivatedArtist()

        mockMvc.perform(
            post("/appointment/test-artist")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validFormBody().toMutableMap().apply { put("description", "too short") }))
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

    // --- GET /appointment/verify ---

    @Test
    fun `verify appointment form sets verifiedAt and returns 204`() {
        val artist = createActivatedArtist()
        mockMvc.perform(
            post("/appointment/${artist.slug}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validFormBody()))
        )
        val form = appointmentFormRepository.findAll().single()
        assertThat(form.verifiedAt).isNull()

        mockMvc.perform(get("/appointment/verify").param("formId", form.id.toString()))
            .andExpect(status().isNoContent)

        assertThat(appointmentFormRepository.findById(form.id).get().verifiedAt).isNotNull()
    }

    @Test
    fun `verify appointment form with unknown id returns 404`() {
        mockMvc.perform(get("/appointment/verify").param("formId", UUID.randomUUID().toString()))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.error").value("Appointment form not found"))
    }
}
