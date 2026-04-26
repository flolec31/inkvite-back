package com.inkvite.inkviteback.email.event

import com.inkvite.inkviteback.appointment.entity.Appointment
import com.inkvite.inkviteback.appointment.event.AppointmentNotificationEmailRequested
import com.inkvite.inkviteback.appointment.event.AppointmentVerificationEmailRequested
import com.inkvite.inkviteback.artist.entity.TattooArtist
import com.inkvite.inkviteback.auth.event.ArtistVerificationEmailRequested
import com.inkvite.inkviteback.client.entity.TattooClient
import com.inkvite.inkviteback.email.service.EmailService
import java.time.Instant
import java.util.UUID
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class EmailEventListenerTest {

    @Mock
    private lateinit var emailService: EmailService

    @InjectMocks
    private lateinit var listener: EmailEventListener

    @Test
    fun `on ArtistVerificationEmailRequested delegates to email service`() {
        val event =
            ArtistVerificationEmailRequested(to = "user@example.com", artistName = "Test Artist", token = "abc123")

        listener.on(event)

        verify(emailService).sendArtistVerificationEmail("user@example.com", "Test Artist", "abc123")
    }

    @Test
    fun `on AppointmentVerificationEmailRequested delegates to email service`() {
        val appointment = buildAppointment()
        val event = AppointmentVerificationEmailRequested(appointment)

        listener.on(event)

        verify(emailService).sendAppointmentVerificationEmail(appointment)
    }

    @Test
    fun `on AppointmentNotificationEmailRequested delegates to email service`() {
        val appointment = buildAppointment()
        val event = AppointmentNotificationEmailRequested(appointment)

        listener.on(event)

        verify(emailService).sendAppointmentNotificationEmail(appointment)
    }

    private fun buildAppointment(): Appointment {
        val artist = TattooArtist(
            id = UUID.randomUUID(),
            email = "artist@test.com",
            password = "hashed",
            artistName = "Test Artist",
            slug = "test-artist",
            registeredAt = Instant.now(),
            activatedAt = Instant.now()
        )
        val client = TattooClient(email = "client@test.com", firstName = "Jane", lastName = "Doe")
        return Appointment(
            artist = artist,
            client = client,
            tattooDescription = "A beautiful dragon tattoo",
            tattooPlacement = "forearm",
            tattooSize = "10x10cm",
            firstTattoo = false,
            coverUp = false
        )
    }
}
