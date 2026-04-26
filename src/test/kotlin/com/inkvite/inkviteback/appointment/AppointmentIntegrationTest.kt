package com.inkvite.inkviteback.appointment

import com.inkvite.inkviteback.AbstractIntegrationTest
import com.inkvite.inkviteback.appointment.entity.Appointment
import com.inkvite.inkviteback.appointment.repository.AppointmentRepository
import com.inkvite.inkviteback.appointment.repository.ReferenceRepository
import com.inkvite.inkviteback.artist.entity.TattooArtist
import com.inkvite.inkviteback.artist.repository.TattooArtistRepository
import com.inkvite.inkviteback.auth.service.JwtService
import com.inkvite.inkviteback.client.entity.TattooClient
import com.inkvite.inkviteback.client.repository.TattooClientRepository
import com.inkvite.inkviteback.email.service.EmailService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.ObjectMapper
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

class AppointmentIntegrationTest : AbstractIntegrationTest() {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var objectMapper: ObjectMapper
    @Autowired lateinit var artistRepository: TattooArtistRepository
    @Autowired lateinit var appointmentRepository: AppointmentRepository
    @Autowired lateinit var tattooClientRepository: TattooClientRepository
    @Autowired lateinit var referenceRepository: ReferenceRepository
    @Autowired lateinit var jwtService: JwtService

    @MockitoBean lateinit var emailService: EmailService

    @BeforeEach
    @AfterEach
    fun cleanup() {
        referenceRepository.deleteAll()
        appointmentRepository.deleteAll()
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

    // --- POST /appointment/{slug}/reference ---

    @Test
    fun `upload reference with jpeg returns 201 with key and signed url`() {
        val artist = createActivatedArtist()

        mockMvc.perform(
            multipart("/appointment/${artist.slug}/reference")
                .file(MockMultipartFile("photo", "ref.jpg", "image/jpeg", ByteArray(100)))
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
                .file(MockMultipartFile("photo", "ref.png", "image/png", ByteArray(100)))
        ).andExpect(status().isCreated)
    }

    @Test
    fun `upload reference with webp returns 201`() {
        val artist = createActivatedArtist()

        mockMvc.perform(
            multipart("/appointment/${artist.slug}/reference")
                .file(MockMultipartFile("photo", "ref.webp", "image/webp", ByteArray(100)))
        ).andExpect(status().isCreated)
    }

    @Test
    fun `upload reference with invalid content type returns 400`() {
        val artist = createActivatedArtist()

        mockMvc.perform(
            multipart("/appointment/${artist.slug}/reference")
                .file(MockMultipartFile("photo", "file.pdf", "application/pdf", ByteArray(100)))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Reference photo must be a JPEG, PNG, or WebP image"))
    }

    @Test
    fun `upload reference exceeding 5mb returns 400`() {
        val artist = createActivatedArtist()

        mockMvc.perform(
            multipart("/appointment/${artist.slug}/reference")
                .file(MockMultipartFile("photo", "big.jpg", "image/jpeg", ByteArray(5 * 1024 * 1024 + 1)))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Reference photo must not exceed 5 MB"))
    }

    @Test
    fun `upload reference for unknown slug returns 404`() {
        mockMvc.perform(
            multipart("/appointment/unknown-slug/reference")
                .file(MockMultipartFile("photo", "ref.jpg", "image/jpeg", ByteArray(100)))
        ).andExpect(status().isNotFound)
    }

    @Test
    fun `upload reference with invalid slug format returns 400`() {
        mockMvc.perform(
            multipart("/appointment/INVALID/reference")
                .file(MockMultipartFile("photo", "ref.jpg", "image/jpeg", ByteArray(100)))
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

        mockMvc.perform(get("/appointment/verify").param("formId", form.id.toString()))
            .andExpect(status().isNoContent)

        assertThat(appointmentRepository.findById(form.id).get().verifiedAt).isNotNull()
    }

    @Test
    fun `verify appointment form with unknown id returns 404`() {
        mockMvc.perform(get("/appointment/verify").param("formId", UUID.randomUUID().toString()))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.error").value("Appointment not found"))
    }

    // --- GET /appointment/ ---

    private fun saveVerifiedAppointment(
        artist: TattooArtist,
        verifiedAt: Instant = Instant.now(),
        clientEmail: String = "client-${UUID.randomUUID()}@test.com"
    ): Appointment {
        val client = tattooClientRepository.save(TattooClient(email = clientEmail, firstName = "Jane", lastName = "Doe"))
        return appointmentRepository.save(
            Appointment(
                artist = artist,
                client = client,
                tattooDescription = "A beautiful dragon tattoo on my forearm",
                tattooPlacement = "forearm",
                tattooSize = "10x10cm",
                firstTattoo = false,
                coverUp = false,
                verifiedAt = verifiedAt
            )
        )
    }

    @Test
    fun `get appointments list returns 401 when not authenticated`() {
        mockMvc.perform(get("/appointment/"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `get appointments list returns empty page when artist has no verified appointments`() {
        val artist = createActivatedArtist()
        val token = jwtService.generateAccessToken(artist.id)

        mockMvc.perform(get("/appointment/").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isEmpty)
            .andExpect(jsonPath("$.total").value(0))
            .andExpect(jsonPath("$.pageCount").value(0))
    }

    @Test
    fun `get appointments list does not return unverified appointments`() {
        val artist = createActivatedArtist()
        val token = jwtService.generateAccessToken(artist.id)
        val client = tattooClientRepository.save(TattooClient(email = "client@test.com", firstName = "Jane", lastName = "Doe"))
        appointmentRepository.save(Appointment(artist = artist, client = client, tattooDescription = "desc", tattooPlacement = "arm", tattooSize = "10x10cm", firstTattoo = false, coverUp = false))

        mockMvc.perform(get("/appointment/").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isEmpty)
            .andExpect(jsonPath("$.total").value(0))
    }

    @Test
    fun `get appointments list returns correct fields for a verified appointment`() {
        val artist = createActivatedArtist()
        val token = jwtService.generateAccessToken(artist.id)
        val appointment = saveVerifiedAppointment(artist)

        mockMvc.perform(get("/appointment/").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].id").value(appointment.id.toString()))
            .andExpect(jsonPath("$.content[0].firstName").value("Jane"))
            .andExpect(jsonPath("$.content[0].lastName").value("Doe"))
            .andExpect(jsonPath("$.content[0].tattooPlacement").value("forearm"))
            .andExpect(jsonPath("$.content[0].description").value("A beautiful dragon tattoo on my forearm"))
            .andExpect(jsonPath("$.content[0].receivedAt").isString)
            .andExpect(jsonPath("$.content[0].new").value(true))
    }

    @Test
    fun `get appointments list returns correct pagination metadata`() {
        val artist = createActivatedArtist()
        val token = jwtService.generateAccessToken(artist.id)
        repeat(3) { saveVerifiedAppointment(artist) }

        mockMvc.perform(get("/appointment/").param("size", "2").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.total").value(3))
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.pageCount").value(2))
    }

    @Test
    fun `get appointments list does not return other artists appointments`() {
        val artist = createActivatedArtist(slug = "my-artist")
        val other = createActivatedArtist(slug = "other-artist")
        val token = jwtService.generateAccessToken(artist.id)
        saveVerifiedAppointment(other)

        mockMvc.perform(get("/appointment/").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isEmpty)
            .andExpect(jsonPath("$.total").value(0))
    }

    @Test
    fun `get appointments list is sorted by verifiedAt descending`() {
        val artist = createActivatedArtist()
        val token = jwtService.generateAccessToken(artist.id)
        val older = saveVerifiedAppointment(artist, verifiedAt = Instant.now().minus(1, ChronoUnit.HOURS))
        val newer = saveVerifiedAppointment(artist, verifiedAt = Instant.now())

        mockMvc.perform(get("/appointment/").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].id").value(newer.id.toString()))
            .andExpect(jsonPath("$.content[1].id").value(older.id.toString()))
    }

    // --- GET /appointment/{appointmentId} ---

    @Test
    fun `get appointment details returns 401 when not authenticated`() {
        mockMvc.perform(get("/appointment/${UUID.randomUUID()}"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `get appointment details returns 404 when appointment does not exist`() {
        val artist = createActivatedArtist()
        val token = jwtService.generateAccessToken(artist.id)

        mockMvc.perform(get("/appointment/${UUID.randomUUID()}").header("Authorization", "Bearer $token"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.error").value("Appointment not found"))
    }

    @Test
    fun `get appointment details returns 404 when appointment is not verified`() {
        val artist = createActivatedArtist()
        val token = jwtService.generateAccessToken(artist.id)
        val client = tattooClientRepository.save(TattooClient(email = "client@test.com", firstName = "Jane", lastName = "Doe"))
        val unverified = appointmentRepository.save(
            Appointment(
                artist = artist,
                client = client,
                tattooDescription = "A tattoo",
                tattooPlacement = "arm",
                tattooSize = "10x10cm",
                firstTattoo = false,
                coverUp = false,
                verifiedAt = null
            )
        )

        mockMvc.perform(get("/appointment/${unverified.id}").header("Authorization", "Bearer $token"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `get appointment details returns 403 when appointment belongs to another artist`() {
        val artist = createActivatedArtist(slug = "my-artist")
        val other = createActivatedArtist(slug = "other-artist")
        val token = jwtService.generateAccessToken(artist.id)
        val appointment = saveVerifiedAppointment(other)

        mockMvc.perform(get("/appointment/${appointment.id}").header("Authorization", "Bearer $token"))
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.error").value("The requested appointment belongs to another artist"))
    }

    @Test
    fun `get appointment details returns all fields for verified appointment`() {
        val artist = createActivatedArtist()
        val token = jwtService.generateAccessToken(artist.id)
        val appointment = saveVerifiedAppointment(artist)

        mockMvc.perform(get("/appointment/${appointment.id}").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(appointment.id.toString()))
            .andExpect(jsonPath("$.tattooDescription").value("A beautiful dragon tattoo on my forearm"))
            .andExpect(jsonPath("$.tattooPlacement").value("forearm"))
            .andExpect(jsonPath("$.tattooSize").value("10x10cm"))
            .andExpect(jsonPath("$.firstTattoo").value(false))
            .andExpect(jsonPath("$.coverUp").value(false))
            .andExpect(jsonPath("$.receivedAt").isString)
            .andExpect(jsonPath("$.clientName").value("Jane Doe"))
            .andExpect(jsonPath("$.clientEmail").value(org.hamcrest.Matchers.containsString("@test.com")))
            .andExpect(jsonPath("$.references").isArray)
    }

    @Test
    fun `get appointment details returns signed urls for references`() {
        val artist = createActivatedArtist()
        val token = jwtService.generateAccessToken(artist.id)
        val appointment = saveVerifiedAppointment(artist)
        referenceRepository.save(com.inkvite.inkviteback.appointment.entity.Reference(
            appointment = appointment,
            key = "references/${artist.id}/ref1.jpg",
            comment = "Like this style"
        ))
        referenceRepository.save(com.inkvite.inkviteback.appointment.entity.Reference(
            appointment = appointment,
            key = "references/${artist.id}/ref2.jpg",
            comment = null
        ))

        mockMvc.perform(get("/appointment/${appointment.id}").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.references.length()").value(2))
            .andExpect(jsonPath("$.references[0].id").isString)
            .andExpect(jsonPath("$.references[0].url").value(org.hamcrest.Matchers.containsString("X-Amz-Signature")))
            .andExpect(jsonPath("$.references[0].comment").value("Like this style"))
            .andExpect(jsonPath("$.references[1].url").value(org.hamcrest.Matchers.containsString("X-Amz-Signature")))
            .andExpect(jsonPath("$.references[1].comment").value(null as String?))
    }

    @Test
    fun `get appointment details marks appointment as seen on first access`() {
        val artist = createActivatedArtist()
        val token = jwtService.generateAccessToken(artist.id)
        val appointment = saveVerifiedAppointment(artist)
        assertThat(appointment.new).isTrue()

        mockMvc.perform(get("/appointment/${appointment.id}").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)

        assertThat(appointmentRepository.findById(appointment.id).get().new).isFalse()
    }

    @Test
    fun `get appointment details does not change new flag on subsequent access`() {
        val artist = createActivatedArtist()
        val token = jwtService.generateAccessToken(artist.id)
        val appointment = saveVerifiedAppointment(artist)

        mockMvc.perform(get("/appointment/${appointment.id}").header("Authorization", "Bearer $token"))
        mockMvc.perform(get("/appointment/${appointment.id}").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)

        assertThat(appointmentRepository.findById(appointment.id).get().new).isFalse()
    }
}
