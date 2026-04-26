package com.inkvite.inkviteback.email.service

import com.inkvite.inkviteback.appointment.entity.Appointment
import com.inkvite.inkviteback.artist.entity.TattooArtist
import com.inkvite.inkviteback.client.entity.TattooClient
import com.inkvite.inkviteback.email.client.ResendEmailClient
import com.inkvite.inkviteback.email.service.implementation.EmailServiceImpl
import java.time.Instant
import java.util.UUID
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class EmailServiceImplTest {

    @Mock
    private lateinit var resendEmailClient: ResendEmailClient

    private lateinit var emailService: EmailServiceImpl

    @BeforeEach
    fun setUp() {
        emailService = EmailServiceImpl(resendEmailClient, "http://localhost:8080")
    }

    @Test
    fun `sendArtistVerificationEmail builds verification link and delegates to client`() {
        emailService.sendArtistVerificationEmail("user@example.com", "Test Artist", "abc123")

        verify(resendEmailClient).sendEmail(
            "user@example.com",
            "verify-artist",
            mapOf(
                "LINK" to "http://localhost:8080/auth/verify?token=abc123",
                "ARTIST_NAME" to "Test Artist"
            )
        )
    }

    @Test
    fun `sendAppointmentVerificationEmail builds verification link and delegates to client`() {
        val appointment = buildAppointment(clientEmail = "client@test.com")

        emailService.sendAppointmentVerificationEmail(appointment)

        verify(resendEmailClient).sendEmail(
            "client@test.com",
            "verify-appointment",
            mapOf(
                "LINK" to "http://localhost:8080/appointment/verify?appointmentId=${appointment.id}",
                "ARTIST_NAME" to "Test Artist",
                "CLIENT_FIRSTNAME" to "Jane"
            )
        )
    }

    @Test
    fun `sendAppointmentNotificationEmail builds dashboard link and delegates to client`() {
        val appointment = buildAppointment(artistEmail = "artist@test.com")

        emailService.sendAppointmentNotificationEmail(appointment)

        verify(resendEmailClient).sendEmail(
            "artist@test.com",
            "new-appointment-notification",
            mapOf(
                "DASHBOARD_LINK" to "http://localhost:8080/dashboard",
                "ARTIST_NAME" to "Test Artist",
                "CLIENT_NAME" to "Jane Doe"
            )
        )
    }

    private fun buildAppointment(
        clientEmail: String = "client@test.com",
        artistEmail: String = "artist@test.com"
    ): Appointment {
        val artist = TattooArtist(
            id = UUID.randomUUID(),
            email = artistEmail,
            password = "hashed",
            artistName = "Test Artist",
            slug = "test-artist",
            registeredAt = Instant.now(),
            activatedAt = Instant.now()
        )
        val client = TattooClient(email = clientEmail, firstName = "Jane", lastName = "Doe")
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
