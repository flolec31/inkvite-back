package com.inkvite.inkviteback.discussion

import com.inkvite.inkviteback.appointment.AbstractAppointmentIntegrationTest
import com.inkvite.inkviteback.appointment.entity.Appointment
import com.inkvite.inkviteback.auth.Role
import com.inkvite.inkviteback.auth.service.JwtService
import com.inkvite.inkviteback.discussion.entity.Message
import com.inkvite.inkviteback.discussion.entity.MessageSender
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

class DiscussionClientIntegrationTest : AbstractAppointmentIntegrationTest() {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var jwtService: JwtService

    private fun saveMessage(appointment: Appointment, sender: MessageSender, content: String? = null, imageKey: String? = null): Message =
        messageRepository.save(Message(appointment = appointment, sender = sender, content = content, imageKey = imageKey))

    // --- GET /client/appointment/{appointmentId}/messages ---

    @Test
    fun `get client messages returns 401 when not authenticated`() {
        mockMvc.perform(get("/client/appointment/${UUID.randomUUID()}/messages"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `get client messages returns 403 when called with an artist token`() {
        val artist = createActivatedArtist()
        val client = createClient()
        val appointment = createAppointment(artist, client)
        val artistToken = jwtService.generateAccessToken(artist.id, Role.ARTIST)

        mockMvc.perform(
            get("/client/appointment/${appointment.id}/messages").header("Authorization", "Bearer $artistToken")
        ).andExpect(status().isForbidden)
    }

    @Test
    fun `get client messages returns 404 when appointment does not exist`() {
        val client = createClient()
        val token = jwtService.generateAccessToken(client.id, Role.CLIENT)

        mockMvc.perform(
            get("/client/appointment/${UUID.randomUUID()}/messages").header("Authorization", "Bearer $token")
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.error").value("Appointment not found"))
    }

    @Test
    fun `get client messages returns 404 when appointment is not verified`() {
        val artist = createActivatedArtist()
        val client = createClient()
        val appointment = createAppointment(artist, client, verifiedAt = null)
        val token = jwtService.generateAccessToken(client.id, Role.CLIENT)

        mockMvc.perform(
            get("/client/appointment/${appointment.id}/messages").header("Authorization", "Bearer $token")
        ).andExpect(status().isNotFound)
    }

    @Test
    fun `get client messages returns 404 when appointment belongs to another client`() {
        val artist = createActivatedArtist()
        val owner = createClient(email = "owner@test.com")
        val other = createClient(email = "other@test.com")
        val appointment = createAppointment(artist, owner)
        saveMessage(appointment, MessageSender.ARTIST, content = "secret")
        val otherToken = jwtService.generateAccessToken(other.id, Role.CLIENT)

        mockMvc.perform(
            get("/client/appointment/${appointment.id}/messages").header("Authorization", "Bearer $otherToken")
        ).andExpect(status().isNotFound)
    }

    @Test
    fun `get client messages returns thread oldest first with both senders`() {
        val artist = createActivatedArtist()
        val client = createClient()
        val appointment = createAppointment(artist, client)
        saveMessage(appointment, MessageSender.ARTIST, content = "first")
        saveMessage(appointment, MessageSender.CLIENT, content = "second")
        val token = jwtService.generateAccessToken(client.id, Role.CLIENT)

        mockMvc.perform(
            get("/client/appointment/${appointment.id}/messages").header("Authorization", "Bearer $token")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].content").value("first"))
            .andExpect(jsonPath("$[0].sender").value("ARTIST"))
            .andExpect(jsonPath("$[1].content").value("second"))
            .andExpect(jsonPath("$[1].sender").value("CLIENT"))
    }

    @Test
    fun `get client messages returns signed imageUrl for image messages`() {
        val artist = createActivatedArtist()
        val client = createClient()
        val appointment = createAppointment(artist, client)
        saveMessage(appointment, MessageSender.ARTIST, imageKey = "messages/${artist.id}/${UUID.randomUUID()}")
        val token = jwtService.generateAccessToken(client.id, Role.CLIENT)

        mockMvc.perform(
            get("/client/appointment/${appointment.id}/messages").header("Authorization", "Bearer $token")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].content").value(null as String?))
            .andExpect(jsonPath("$[0].imageUrl").value(containsString("X-Amz-Signature")))
    }

    @Test
    fun `get client messages returns empty array when no messages`() {
        val artist = createActivatedArtist()
        val client = createClient()
        val appointment = createAppointment(artist, client)
        val token = jwtService.generateAccessToken(client.id, Role.CLIENT)

        mockMvc.perform(
            get("/client/appointment/${appointment.id}/messages").header("Authorization", "Bearer $token")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$.length()").value(0))
    }
}
