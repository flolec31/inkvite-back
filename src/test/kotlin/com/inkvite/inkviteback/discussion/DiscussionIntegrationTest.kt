package com.inkvite.inkviteback.discussion

import com.inkvite.inkviteback.appointment.AbstractAppointmentIntegrationTest
import com.inkvite.inkviteback.appointment.entity.Appointment
import com.inkvite.inkviteback.artist.entity.TattooArtist
import com.inkvite.inkviteback.auth.service.JwtService
import com.inkvite.inkviteback.client.entity.TattooClient
import com.inkvite.inkviteback.discussion.entity.MessageSender
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant
import java.util.UUID

class DiscussionIntegrationTest : AbstractAppointmentIntegrationTest() {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var jwtService: JwtService

    private fun saveVerifiedAppointment(artist: TattooArtist): Appointment {
        val client = tattooClientRepository.save(TattooClient(email = "client-${UUID.randomUUID()}@test.com", firstName = "Jane", lastName = "Doe"))
        return appointmentRepository.save(
            Appointment(
                artist = artist, client = client,
                tattooDescription = "desc", tattooPlacement = "arm", tattooSize = "10x10cm",
                firstTattoo = false, coverUp = false, verifiedAt = Instant.now()
            )
        )
    }

    // --- POST /appointment/{appointmentId}/messages ---

    @Test
    fun `post message returns 201 and persists an artist message`() {
        val artist = createActivatedArtist()
        val token = jwtService.generateAccessToken(artist.id)
        val appointment = saveVerifiedAppointment(artist)

        mockMvc.perform(
            post("/appointment/${appointment.id}/messages")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"content":"Hello there"}""")
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").isString)
            .andExpect(jsonPath("$.sender").value("ARTIST"))
            .andExpect(jsonPath("$.content").value("Hello there"))
            .andExpect(jsonPath("$.sentAt").isString)
            .andExpect(jsonPath("$.readAt").value(null as String?))

        val persisted = messageRepository.findByAppointmentIdOrderBySentAtAsc(appointment.id)
        assertThat(persisted).hasSize(1)
        assertThat(persisted[0].sender).isEqualTo(MessageSender.ARTIST)
        assertThat(persisted[0].content).isEqualTo("Hello there")
        assertThat(persisted[0].readAt).isNull()
    }

    @Test
    fun `post message returns 400 when content is blank`() {
        val artist = createActivatedArtist()
        val token = jwtService.generateAccessToken(artist.id)
        val appointment = saveVerifiedAppointment(artist)

        mockMvc.perform(
            post("/appointment/${appointment.id}/messages")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"content":"   "}""")
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `post message returns 400 when content exceeds max length`() {
        val artist = createActivatedArtist()
        val token = jwtService.generateAccessToken(artist.id)
        val appointment = saveVerifiedAppointment(artist)

        mockMvc.perform(
            post("/appointment/${appointment.id}/messages")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"content":"${"a".repeat(2001)}"}""")
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `post message returns 401 when not authenticated`() {
        mockMvc.perform(
            post("/appointment/${UUID.randomUUID()}/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"content":"hi"}""")
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `post message returns 404 when appointment does not exist`() {
        val artist = createActivatedArtist()
        val token = jwtService.generateAccessToken(artist.id)

        mockMvc.perform(
            post("/appointment/${UUID.randomUUID()}/messages")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"content":"hi"}""")
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.error").value("Appointment not found"))
    }

    @Test
    fun `post message returns 403 when appointment belongs to another artist`() {
        val artist = createActivatedArtist(slug = "my-artist")
        val other = createActivatedArtist(slug = "other-artist")
        val token = jwtService.generateAccessToken(artist.id)
        val appointment = saveVerifiedAppointment(other)

        mockMvc.perform(
            post("/appointment/${appointment.id}/messages")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"content":"hi"}""")
        )
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("$.error").value("The requested appointment belongs to another artist"))
    }

    // --- GET /appointment/{appointmentId}/messages ---

    @Test
    fun `get messages returns thread oldest first`() {
        val artist = createActivatedArtist()
        val token = jwtService.generateAccessToken(artist.id)
        val appointment = saveVerifiedAppointment(artist)
        mockMvc.perform(post("/appointment/${appointment.id}/messages").header("Authorization", "Bearer $token")
            .contentType(MediaType.APPLICATION_JSON).content("""{"content":"first"}"""))
        mockMvc.perform(post("/appointment/${appointment.id}/messages").header("Authorization", "Bearer $token")
            .contentType(MediaType.APPLICATION_JSON).content("""{"content":"second"}"""))

        mockMvc.perform(get("/appointment/${appointment.id}/messages").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].content").value("first"))
            .andExpect(jsonPath("$[1].content").value("second"))
            .andExpect(jsonPath("$[0].sender").value("ARTIST"))
    }

    @Test
    fun `get messages returns empty array when no messages`() {
        val artist = createActivatedArtist()
        val token = jwtService.generateAccessToken(artist.id)
        val appointment = saveVerifiedAppointment(artist)

        mockMvc.perform(get("/appointment/${appointment.id}/messages").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$.length()").value(0))
    }

    @Test
    fun `get messages returns 401 when not authenticated`() {
        mockMvc.perform(get("/appointment/${UUID.randomUUID()}/messages"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `get messages returns 403 when appointment belongs to another artist`() {
        val artist = createActivatedArtist(slug = "my-artist")
        val other = createActivatedArtist(slug = "other-artist")
        val token = jwtService.generateAccessToken(artist.id)
        val appointment = saveVerifiedAppointment(other)

        mockMvc.perform(get("/appointment/${appointment.id}/messages").header("Authorization", "Bearer $token"))
            .andExpect(status().isForbidden)
    }
}
