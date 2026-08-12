package com.inkvite.inkviteback.discussion

import com.inkvite.inkviteback.appointment.AbstractAppointmentIntegrationTest
import com.inkvite.inkviteback.appointment.entity.Appointment
import com.inkvite.inkviteback.artist.entity.TattooArtist
import com.inkvite.inkviteback.client.entity.TattooClient
import com.inkvite.inkviteback.discussion.entity.Message
import com.inkvite.inkviteback.discussion.entity.MessageSender
import com.inkvite.inkviteback.discussion.repository.MessageRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant
import java.time.temporal.ChronoUnit

class MessageRepositoryTest : AbstractAppointmentIntegrationTest() {

    private fun saveAppointment(artist: TattooArtist): Appointment {
        val client = tattooClientRepository.save(TattooClient(email = "c@test.com", firstName = "Jane", lastName = "Doe"))
        return appointmentRepository.save(
            Appointment(
                artist = artist, client = client,
                tattooDescription = "desc", tattooPlacement = "arm", tattooSize = "10x10cm",
                firstTattoo = false, coverUp = false, verifiedAt = Instant.now()
            )
        )
    }

    @Test
    fun `findByAppointmentIdOrderBySentAtAsc returns messages oldest first`() {
        val artist = createActivatedArtist()
        val appointment = saveAppointment(artist)
        val now = Instant.now()
        messageRepository.save(Message(appointment = appointment, sender = MessageSender.ARTIST, content = "second", sentAt = now))
        messageRepository.save(Message(appointment = appointment, sender = MessageSender.ARTIST, content = "first", sentAt = now.minus(1, ChronoUnit.HOURS)))

        val result = messageRepository.findByAppointmentIdOrderBySentAtAsc(appointment.id)

        assertThat(result).extracting<String> { it.content }.containsExactly("first", "second")
        assertThat(result[0].sender).isEqualTo(MessageSender.ARTIST)
        assertThat(result[0].readAt).isNull()
    }

    @Test
    fun `findByAppointmentIdOrderBySentAtAsc returns empty list for appointment with no messages`() {
        val artist = createActivatedArtist()
        val appointment = saveAppointment(artist)

        assertThat(messageRepository.findByAppointmentIdOrderBySentAtAsc(appointment.id)).isEmpty()
    }
}
