package com.inkvite.inkviteback.appointment

import com.inkvite.inkviteback.appointment.entity.Appointment
import com.inkvite.inkviteback.artist.entity.TattooArtist
import com.inkvite.inkviteback.auth.service.JwtService
import com.inkvite.inkviteback.client.entity.TattooClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

class AppointmentManagementIntegrationTest : AbstractAppointmentIntegrationTest() {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var jwtService: JwtService

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

    // --- GET /appointment ---

    @Test
    fun `get appointments list returns 401 when not authenticated`() {
        mockMvc.perform(get("/appointment/"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `get appointments list returns empty page when artist has no verified appointments`() {
        val artist = createActivatedArtist()
        val token = jwtService.generateAccessToken(artist.id)

        mockMvc.perform(get("/appointment").header("Authorization", "Bearer $token"))
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

        mockMvc.perform(get("/appointment").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isEmpty)
            .andExpect(jsonPath("$.total").value(0))
    }

    @Test
    fun `get appointments list returns correct fields for a verified appointment`() {
        val artist = createActivatedArtist()
        val token = jwtService.generateAccessToken(artist.id)
        val appointment = saveVerifiedAppointment(artist)

        mockMvc.perform(get("/appointment").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].id").value(appointment.id.toString()))
            .andExpect(jsonPath("$.content[0].firstName").value("Jane"))
            .andExpect(jsonPath("$.content[0].lastName").value("Doe"))
            .andExpect(jsonPath("$.content[0].tattooPlacement").value("forearm"))
            .andExpect(jsonPath("$.content[0].description").value("A beautiful dragon tattoo on my forearm"))
            .andExpect(jsonPath("$.content[0].receivedAt").isString)
            .andExpect(jsonPath("$.content[0].new").value(true))
            .andExpect(jsonPath("$.content[0].archived").value(false))
    }

    @Test
    fun `get appointments list returns correct pagination metadata`() {
        val artist = createActivatedArtist()
        val token = jwtService.generateAccessToken(artist.id)
        repeat(3) { saveVerifiedAppointment(artist) }

        mockMvc.perform(get("/appointment").param("size", "2").header("Authorization", "Bearer $token"))
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

        mockMvc.perform(get("/appointment").header("Authorization", "Bearer $token"))
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

        mockMvc.perform(get("/appointment").header("Authorization", "Bearer $token"))
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
            .andExpect(jsonPath("$.clientEmail").doesNotExist())
            .andExpect(jsonPath("$.references").isArray)
            .andExpect(jsonPath("$.new").value(false))
            .andExpect(jsonPath("$.archived").value(false))
    }

    @Test
    fun `get appointment details returns signed urls for references`() {
        val artist = createActivatedArtist()
        val token = jwtService.generateAccessToken(artist.id)
        val appointment = saveVerifiedAppointment(artist)
        referenceRepository.save(com.inkvite.inkviteback.appointment.entity.Reference(
            appointment = appointment,
            key = "references/${artist.id}/ref1.jpg",
            comment = "Like this style",
            cropLeft = 232, cropTop = 309, cropWidth = 1853, cropHeight = 2470,
        ))
        referenceRepository.save(com.inkvite.inkviteback.appointment.entity.Reference(
            appointment = appointment,
            key = "references/${artist.id}/ref2.jpg",
            comment = null,
            cropLeft = 0, cropTop = 0, cropWidth = 800, cropHeight = 600,
        ))

        mockMvc.perform(get("/appointment/${appointment.id}").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.references.length()").value(2))
            .andExpect(jsonPath("$.references[0].id").isString)
            .andExpect(jsonPath("$.references[0].url").value(org.hamcrest.Matchers.containsString("X-Amz-Signature")))
            .andExpect(jsonPath("$.references[0].comment").value("Like this style"))
            .andExpect(jsonPath("$.references[0].crop.left").value(232))
            .andExpect(jsonPath("$.references[0].crop.top").value(309))
            .andExpect(jsonPath("$.references[0].crop.width").value(1853))
            .andExpect(jsonPath("$.references[0].crop.height").value(2470))
            .andExpect(jsonPath("$.references[1].url").value(org.hamcrest.Matchers.containsString("X-Amz-Signature")))
            .andExpect(jsonPath("$.references[1].comment").value(null as String?))
    }

    @Test
    fun `get appointment details transitions new from true to false on first access`() {
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

    // --- POST /appointment/{appointmentId}/archive ---

    @Test
    fun `archive appointment returns 401 when not authenticated`() {
        mockMvc.perform(post("/appointment/${UUID.randomUUID()}/archive"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `archive appointment returns 404 when appointment does not exist`() {
        val artist = createActivatedArtist()
        val token = jwtService.generateAccessToken(artist.id)

        mockMvc.perform(post("/appointment/${UUID.randomUUID()}/archive").header("Authorization", "Bearer $token"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.error").value("Appointment not found"))
    }

    @Test
    fun `archive appointment returns 403 when appointment belongs to another artist`() {
        val artist = createActivatedArtist(slug = "my-artist")
        val other = createActivatedArtist(slug = "other-artist")
        val token = jwtService.generateAccessToken(artist.id)
        val appointment = saveVerifiedAppointment(other)

        mockMvc.perform(post("/appointment/${appointment.id}/archive").header("Authorization", "Bearer $token"))
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.error").value("The requested appointment belongs to another artist"))
    }

    @Test
    fun `archive appointment that is already archived returns 409`() {
        val artist = createActivatedArtist()
        val token = jwtService.generateAccessToken(artist.id)
        val appointment = saveVerifiedAppointment(artist)
        appointment.archived = true
        appointmentRepository.save(appointment)

        mockMvc.perform(post("/appointment/${appointment.id}/archive").header("Authorization", "Bearer $token"))
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.error").value("Appointment is already archived"))
    }

    @Test
    fun `archive appointment marks it archived without changing new flag`() {
        val artist = createActivatedArtist()
        val token = jwtService.generateAccessToken(artist.id)
        val appointment = saveVerifiedAppointment(artist)

        mockMvc.perform(post("/appointment/${appointment.id}/archive").header("Authorization", "Bearer $token"))
            .andExpect(status().isNoContent)

        val persisted = appointmentRepository.findById(appointment.id).get()
        assertThat(persisted.archived).isTrue()
        assertThat(persisted.new).isTrue()
    }

    @Test
    fun `archive appointment preserves its current new flag so it can be restored later`() {
        val artist = createActivatedArtist()
        val token = jwtService.generateAccessToken(artist.id)
        val appointment = saveVerifiedAppointment(artist)
        appointment.new = false
        appointmentRepository.save(appointment)

        mockMvc.perform(post("/appointment/${appointment.id}/archive").header("Authorization", "Bearer $token"))
            .andExpect(status().isNoContent)

        assertThat(appointmentRepository.findById(appointment.id).get().new).isFalse()
    }

    // --- POST /appointment/{appointmentId}/unarchive ---

    @Test
    fun `unarchive appointment returns 401 when not authenticated`() {
        mockMvc.perform(post("/appointment/${UUID.randomUUID()}/unarchive"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `unarchive appointment returns 404 when appointment does not exist`() {
        val artist = createActivatedArtist()
        val token = jwtService.generateAccessToken(artist.id)

        mockMvc.perform(post("/appointment/${UUID.randomUUID()}/unarchive").header("Authorization", "Bearer $token"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.error").value("Appointment not found"))
    }

    @Test
    fun `unarchive appointment returns 403 when appointment belongs to another artist`() {
        val artist = createActivatedArtist(slug = "my-artist")
        val other = createActivatedArtist(slug = "other-artist")
        val token = jwtService.generateAccessToken(artist.id)
        val appointment = saveVerifiedAppointment(other)
        appointment.archived = true
        appointmentRepository.save(appointment)

        mockMvc.perform(post("/appointment/${appointment.id}/unarchive").header("Authorization", "Bearer $token"))
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.error").value("The requested appointment belongs to another artist"))
    }

    @Test
    fun `unarchive appointment that is not archived returns 409`() {
        val artist = createActivatedArtist()
        val token = jwtService.generateAccessToken(artist.id)
        val appointment = saveVerifiedAppointment(artist)

        mockMvc.perform(post("/appointment/${appointment.id}/unarchive").header("Authorization", "Bearer $token"))
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.error").value("Appointment is not archived"))
    }

    @Test
    fun `unarchive appointment restores it without changing new flag`() {
        val artist = createActivatedArtist()
        val token = jwtService.generateAccessToken(artist.id)
        val appointment = saveVerifiedAppointment(artist)
        appointment.new = false
        appointment.archived = true
        appointmentRepository.save(appointment)

        mockMvc.perform(post("/appointment/${appointment.id}/unarchive").header("Authorization", "Bearer $token"))
            .andExpect(status().isNoContent)

        val persisted = appointmentRepository.findById(appointment.id).get()
        assertThat(persisted.archived).isFalse()
        assertThat(persisted.new).isFalse()
    }

    // --- POST /appointment/{appointmentId}/mark-new ---

    @Test
    fun `mark appointment as new returns 401 when not authenticated`() {
        mockMvc.perform(post("/appointment/${UUID.randomUUID()}/mark-new"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `mark appointment as new returns 404 when appointment does not exist`() {
        val artist = createActivatedArtist()
        val token = jwtService.generateAccessToken(artist.id)

        mockMvc.perform(post("/appointment/${UUID.randomUUID()}/mark-new").header("Authorization", "Bearer $token"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.error").value("Appointment not found"))
    }

    @Test
    fun `mark appointment as new returns 403 when appointment belongs to another artist`() {
        val artist = createActivatedArtist(slug = "my-artist")
        val other = createActivatedArtist(slug = "other-artist")
        val token = jwtService.generateAccessToken(artist.id)
        val appointment = saveVerifiedAppointment(other)
        appointment.new = false
        appointmentRepository.save(appointment)

        mockMvc.perform(post("/appointment/${appointment.id}/mark-new").header("Authorization", "Bearer $token"))
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.error").value("The requested appointment belongs to another artist"))
    }

    @Test
    fun `mark appointment as new that is already new returns 409`() {
        val artist = createActivatedArtist()
        val token = jwtService.generateAccessToken(artist.id)
        val appointment = saveVerifiedAppointment(artist)
        assertThat(appointment.new).isTrue()

        mockMvc.perform(post("/appointment/${appointment.id}/mark-new").header("Authorization", "Bearer $token"))
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.error").value("Appointment is already marked as new"))
    }

    @Test
    fun `mark appointment as new returns 409 when appointment is archived`() {
        val artist = createActivatedArtist()
        val token = jwtService.generateAccessToken(artist.id)
        val appointment = saveVerifiedAppointment(artist)
        appointment.new = false
        appointment.archived = true
        appointmentRepository.save(appointment)

        mockMvc.perform(post("/appointment/${appointment.id}/mark-new").header("Authorization", "Bearer $token"))
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.error").value("Cannot mark an archived appointment as new"))
    }

    @Test
    fun `mark appointment as new sets new flag without changing archived flag`() {
        val artist = createActivatedArtist()
        val token = jwtService.generateAccessToken(artist.id)
        val appointment = saveVerifiedAppointment(artist)
        appointment.new = false
        appointmentRepository.save(appointment)

        mockMvc.perform(post("/appointment/${appointment.id}/mark-new").header("Authorization", "Bearer $token"))
            .andExpect(status().isNoContent)

        val persisted = appointmentRepository.findById(appointment.id).get()
        assertThat(persisted.new).isTrue()
        assertThat(persisted.archived).isFalse()
    }
}
