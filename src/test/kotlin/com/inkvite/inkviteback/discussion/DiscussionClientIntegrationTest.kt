package com.inkvite.inkviteback.discussion

import com.inkvite.inkviteback.appointment.AbstractAppointmentIntegrationTest
import com.inkvite.inkviteback.appointment.entity.Appointment
import com.inkvite.inkviteback.auth.Role
import com.inkvite.inkviteback.auth.service.JwtService
import com.inkvite.inkviteback.discussion.entity.Message
import com.inkvite.inkviteback.discussion.entity.MessageSender
import com.inkvite.inkviteback.email.service.EmailService
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.mockito.kotlin.argThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

class DiscussionClientIntegrationTest : AbstractAppointmentIntegrationTest() {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var jwtService: JwtService
    @MockitoBean lateinit var emailService: EmailService

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

    @Test
    fun `get client messages marks the artist messages read and leaves the client own unread`() {
        val artist = createActivatedArtist()
        val client = createClient()
        val appointment = createAppointment(artist, client)
        saveMessage(appointment, MessageSender.ARTIST, content = "from artist")
        saveMessage(appointment, MessageSender.CLIENT, content = "from client")
        val token = jwtService.generateAccessToken(client.id, Role.CLIENT)

        mockMvc.perform(
            get("/client/appointment/${appointment.id}/messages").header("Authorization", "Bearer $token")
        ).andExpect(status().isOk)

        val persisted = messageRepository.findByAppointmentIdOrderBySentAtAsc(appointment.id)
        assertThat(persisted.single { it.sender == MessageSender.ARTIST }.readAt).isNotNull()
        assertThat(persisted.single { it.sender == MessageSender.CLIENT }.readAt).isNull()
    }

    // --- POST /client/appointment/{appointmentId}/messages ---

    @Test
    fun `post client message returns 201 and persists a client message and notifies the artist`() {
        val artist = createActivatedArtist()
        val client = createClient()
        val appointment = createAppointment(artist, client)
        val token = jwtService.generateAccessToken(client.id, Role.CLIENT)

        mockMvc.perform(
            post("/client/appointment/${appointment.id}/messages")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"content":"Hi from client"}""")
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").isString)
            .andExpect(jsonPath("$.sender").value("CLIENT"))
            .andExpect(jsonPath("$.content").value("Hi from client"))
            .andExpect(jsonPath("$.sentAt").isString)
            .andExpect(jsonPath("$.readAt").value(null as String?))

        val persisted = messageRepository.findByAppointmentIdOrderBySentAtAsc(appointment.id)
        assertThat(persisted).hasSize(1)
        assertThat(persisted[0].sender).isEqualTo(MessageSender.CLIENT)
        assertThat(persisted[0].content).isEqualTo("Hi from client")
        assertThat(persisted[0].readAt).isNull()
        verify(emailService).sendNewMessageEmailToArtist(argThat { id == appointment.id })
    }

    @Test
    fun `post client message returns 400 when content is blank`() {
        val artist = createActivatedArtist()
        val client = createClient()
        val appointment = createAppointment(artist, client)
        val token = jwtService.generateAccessToken(client.id, Role.CLIENT)

        mockMvc.perform(
            post("/client/appointment/${appointment.id}/messages")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"content":"   "}""")
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `post client message returns 400 when neither content nor image is present`() {
        val artist = createActivatedArtist()
        val client = createClient()
        val appointment = createAppointment(artist, client)
        val token = jwtService.generateAccessToken(client.id, Role.CLIENT)

        mockMvc.perform(
            post("/client/appointment/${appointment.id}/messages")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{}""")
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun `post client message returns 401 when not authenticated`() {
        mockMvc.perform(
            post("/client/appointment/${UUID.randomUUID()}/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"content":"hi"}""")
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `post client message returns 403 when called with an artist token`() {
        val artist = createActivatedArtist()
        val client = createClient()
        val appointment = createAppointment(artist, client)
        val artistToken = jwtService.generateAccessToken(artist.id, Role.ARTIST)

        mockMvc.perform(
            post("/client/appointment/${appointment.id}/messages")
                .header("Authorization", "Bearer $artistToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"content":"hi"}""")
        ).andExpect(status().isForbidden)
    }

    @Test
    fun `post client message returns 404 when appointment does not exist`() {
        val client = createClient()
        val token = jwtService.generateAccessToken(client.id, Role.CLIENT)

        mockMvc.perform(
            post("/client/appointment/${UUID.randomUUID()}/messages")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"content":"hi"}""")
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.error").value("Appointment not found"))
    }

    @Test
    fun `post client message returns 404 when appointment is not verified`() {
        val artist = createActivatedArtist()
        val client = createClient()
        val appointment = createAppointment(artist, client, verifiedAt = null)
        val token = jwtService.generateAccessToken(client.id, Role.CLIENT)

        mockMvc.perform(
            post("/client/appointment/${appointment.id}/messages")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"content":"hi"}""")
        ).andExpect(status().isNotFound)
    }

    @Test
    fun `post client message returns 404 when appointment belongs to another client`() {
        val artist = createActivatedArtist()
        val owner = createClient(email = "owner@test.com")
        val other = createClient(email = "other@test.com")
        val appointment = createAppointment(artist, owner)
        val otherToken = jwtService.generateAccessToken(other.id, Role.CLIENT)

        mockMvc.perform(
            post("/client/appointment/${appointment.id}/messages")
                .header("Authorization", "Bearer $otherToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"content":"hi"}""")
        ).andExpect(status().isNotFound)
    }

    @Test
    fun `post image-only client message returns 201 and persists imageKey with signed url`() {
        val artist = createActivatedArtist()
        val client = createClient()
        val appointment = createAppointment(artist, client)
        val token = jwtService.generateAccessToken(client.id, Role.CLIENT)
        val imageKey = "messages/${client.id}/${UUID.randomUUID()}"

        mockMvc.perform(
            post("/client/appointment/${appointment.id}/messages")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"imageKey":"$imageKey"}""")
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.content").value(null as String?))
            .andExpect(jsonPath("$.imageUrl").value(containsString("X-Amz-Signature")))

        val persisted = messageRepository.findByAppointmentIdOrderBySentAtAsc(appointment.id).single()
        assertThat(persisted.content).isNull()
        assertThat(persisted.imageKey).isEqualTo(imageKey)
    }

    @Test
    fun `post client message with content and image returns 201 and persists both`() {
        val artist = createActivatedArtist()
        val client = createClient()
        val appointment = createAppointment(artist, client)
        val token = jwtService.generateAccessToken(client.id, Role.CLIENT)
        val imageKey = "messages/${client.id}/${UUID.randomUUID()}"

        mockMvc.perform(
            post("/client/appointment/${appointment.id}/messages")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"content":"look at this","imageKey":"$imageKey"}""")
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.content").value("look at this"))
            .andExpect(jsonPath("$.imageUrl").value(containsString("X-Amz-Signature")))

        val persisted = messageRepository.findByAppointmentIdOrderBySentAtAsc(appointment.id).single()
        assertThat(persisted.content).isEqualTo("look at this")
        assertThat(persisted.imageKey).isEqualTo(imageKey)
    }

    @Test
    fun `post client message returns 400 when imageKey belongs to another client`() {
        val artist = createActivatedArtist()
        val client = createClient()
        val appointment = createAppointment(artist, client)
        val token = jwtService.generateAccessToken(client.id, Role.CLIENT)
        val foreignKey = "messages/${UUID.randomUUID()}/${UUID.randomUUID()}"

        mockMvc.perform(
            post("/client/appointment/${appointment.id}/messages")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"imageKey":"$foreignKey"}""")
        )
            .andExpect(status().isBadRequest)
    }

    // --- POST /client/appointment/{appointmentId}/messages/image ---

    @Test
    fun `upload client message image returns 201 with key under messages prefix and signed url`() {
        val artist = createActivatedArtist()
        val client = createClient()
        val appointment = createAppointment(artist, client)
        val token = jwtService.generateAccessToken(client.id, Role.CLIENT)

        mockMvc.perform(
            multipart("/client/appointment/${appointment.id}/messages/image")
                .file(MockMultipartFile("image", "pic.jpg", "image/jpeg", ByteArray(100)))
                .header("Authorization", "Bearer $token")
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.key").value(containsString("messages/${client.id}/")))
            .andExpect(jsonPath("$.url").value(containsString("X-Amz-Signature")))
    }

    @Test
    fun `upload client message image returns 400 for invalid content type`() {
        val artist = createActivatedArtist()
        val client = createClient()
        val appointment = createAppointment(artist, client)
        val token = jwtService.generateAccessToken(client.id, Role.CLIENT)

        mockMvc.perform(
            multipart("/client/appointment/${appointment.id}/messages/image")
                .file(MockMultipartFile("image", "file.txt", "text/plain", ByteArray(10)))
                .header("Authorization", "Bearer $token")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Image must be a JPEG, PNG, or WebP image"))
    }

    @Test
    fun `upload client message image returns 404 when appointment belongs to another client`() {
        val artist = createActivatedArtist()
        val owner = createClient(email = "owner@test.com")
        val other = createClient(email = "other@test.com")
        val appointment = createAppointment(artist, owner)
        val otherToken = jwtService.generateAccessToken(other.id, Role.CLIENT)

        mockMvc.perform(
            multipart("/client/appointment/${appointment.id}/messages/image")
                .file(MockMultipartFile("image", "pic.jpg", "image/jpeg", ByteArray(100)))
                .header("Authorization", "Bearer $otherToken")
        ).andExpect(status().isNotFound)
    }

    @Test
    fun `upload client message image returns 401 when not authenticated`() {
        mockMvc.perform(
            multipart("/client/appointment/${UUID.randomUUID()}/messages/image")
                .file(MockMultipartFile("image", "pic.jpg", "image/jpeg", ByteArray(100)))
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `upload client message image returns 403 when called with an artist token`() {
        val artist = createActivatedArtist()
        val client = createClient()
        val appointment = createAppointment(artist, client)
        val artistToken = jwtService.generateAccessToken(artist.id, Role.ARTIST)

        mockMvc.perform(
            multipart("/client/appointment/${appointment.id}/messages/image")
                .file(MockMultipartFile("image", "pic.jpg", "image/jpeg", ByteArray(100)))
                .header("Authorization", "Bearer $artistToken")
        ).andExpect(status().isForbidden)
    }
}
