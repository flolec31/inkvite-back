package com.inkvite.inkviteback.email.service

import com.inkvite.inkviteback.appointment.entity.Appointment
import com.inkvite.inkviteback.appointment.entity.TattooStyle
import com.inkvite.inkviteback.artist.entity.TattooArtist
import com.inkvite.inkviteback.client.entity.TattooClient
import com.inkvite.inkviteback.email.client.ResendEmailClient
import com.inkvite.inkviteback.email.service.implementation.EmailServiceImpl
import com.inkvite.inkviteback.support.entity.SupportMessage
import com.inkvite.inkviteback.support.entity.SupportMessageType
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
        emailService = EmailServiceImpl(resendEmailClient, "http://localhost:8080", "support@inkvite.me")
    }

    @Test
    fun `sendArtistVerificationEmail builds verification link and delegates to client`() {
        emailService.sendArtistVerificationEmail("user@example.com", "Test Artist", "abc123")

        verify(resendEmailClient).sendEmail(
            "user@example.com",
            "verify-artist-signup",
            mapOf(
                "LINK" to "http://localhost:8080/sign-up/verify?token=abc123",
                "ARTIST_NAME" to "Test Artist"
            )
        )
    }

    @Test
    fun `sendPasswordResetEmail builds reset link and delegates to client`() {
        emailService.sendPasswordResetEmail("user@example.com", "Test Artist", "reset-token-123")

        verify(resendEmailClient).sendEmail(
            "user@example.com",
            "verify-reset-password-3",
            mapOf(
                "LINK" to "http://localhost:8080/reset-password?token=reset-token-123",
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
            "verify-appointment-request",
            mapOf(
                "LINK" to "http://localhost:8080/@test-artist/verify?appointmentId=${appointment.id}",
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
            "notify-artist-new-appointment-request",
            mapOf(
                "LINK" to "http://localhost:8080/dashboard",
                "ARTIST_NAME" to "Test Artist",
                "CLIENT_NAME" to "Jane Doe"
            )
        )
    }

    @Test
    fun `sendAppointmentRequestConfirmationEmail builds access link and delegates to client`() {
        val appointment = buildAppointment(clientEmail = "client@test.com")

        emailService.sendAppointmentRequestConfirmationEmail(appointment)

        verify(resendEmailClient).sendEmail(
            "client@test.com",
            "confirm-client-appointment-request",
            mapOf(
                "LINK" to "http://localhost:8080/appointment/${appointment.id}",
                "ARTIST_NAME" to "Test Artist",
                "CLIENT_FIRSTNAME" to "Jane"
            )
        )
    }

    @Test
    fun `sendNewMessageEmailToClient delegates to client with artist and client names`() {
        val appointment = buildAppointment(clientEmail = "client@test.com")

        emailService.sendNewMessageEmailToClient(appointment)

        verify(resendEmailClient).sendEmail(
            "client@test.com",
            "notify-client-new-message",
            mapOf(
                "ARTIST_NAME" to "Test Artist",
                "CLIENT_FIRSTNAME" to "Jane"
            )
        )
    }

    @Test
    fun `sendSupportMessageReceivedEmail sends to the configured notification address`() {
        val artist = TattooArtist(
            id = UUID.randomUUID(),
            email = "artist@test.com",
            password = "hashed",
            artistName = "Test Artist",
            slug = "test-artist",
            city = "Test City",
            countryCode = "FR",
            registeredAt = Instant.now(),
            activatedAt = Instant.now()
        )
        val supportMessage = SupportMessage(
            artist = artist,
            type = SupportMessageType.BUG,
            message = "The upload button is broken",
            createdAt = Instant.now(),
        )

        emailService.sendSupportMessageReceivedEmail(supportMessage)

        verify(resendEmailClient).sendEmail(
            "support@inkvite.me",
            "new-support-message",
            mapOf(
                "TYPE" to "BUG",
                "ARTIST_NAME" to "Test Artist",
                "ARTIST_EMAIL" to "artist@test.com",
                "MESSAGE" to "The upload button is broken"
            )
        )
    }

    @Test
    fun `sendAppointmentLinksEmail builds one link per appointment and delegates to client`() {
        val artist = buildArtist()
        val client = TattooClient(email = "client@test.com", firstName = "Jane", lastName = "Doe")
        val first = buildAppointment(
            artist = artist,
            client = client,
            description = "A beautiful dragon tattoo",
            submittedAt = Instant.parse("2026-09-09T10:00:00Z")
        )
        val second = buildAppointment(
            artist = artist,
            client = client,
            description = "A small rose on the wrist",
            submittedAt = Instant.parse("2026-03-01T08:00:00Z")
        )

        emailService.sendAppointmentLinksEmail(listOf(first, second))

        verify(resendEmailClient).sendEmail(
            "client@test.com",
            "retrieve-appointment-links",
            mapOf(
                "ARTIST_NAME" to "Test Artist",
                "CLIENT_FIRSTNAME" to "Jane",
                "APPOINTMENTS_HTML" to (
                    "<ul>" +
                        "<li><a href=\"http://localhost:8080/appointment/${first.id}\">09/09/26 &ndash; &laquo; A beautiful dragon tattoo &raquo;</a></li>" +
                        "<li><a href=\"http://localhost:8080/appointment/${second.id}\">01/03/26 &ndash; &laquo; A small rose on the wrist &raquo;</a></li>" +
                        "</ul>"
                    )
            )
        )
    }

    @Test
    fun `sendAppointmentLinksEmail escapes HTML in the tattoo description`() {
        val appointment = buildAppointment(
            description = "A <script> & \"quotes\"",
            submittedAt = Instant.parse("2026-09-09T10:00:00Z")
        )

        emailService.sendAppointmentLinksEmail(listOf(appointment))

        verify(resendEmailClient).sendEmail(
            "client@test.com",
            "retrieve-appointment-links",
            mapOf(
                "ARTIST_NAME" to "Test Artist",
                "CLIENT_FIRSTNAME" to "Jane",
                "APPOINTMENTS_HTML" to (
                    "<ul>" +
                        "<li><a href=\"http://localhost:8080/appointment/${appointment.id}\">" +
                        "09/09/26 &ndash; &laquo; A &lt;script&gt; &amp; &quot;quotes&quot; &raquo;</a></li>" +
                        "</ul>"
                    )
            )
        )
    }

    @Test
    fun `sendAppointmentLinksEmail truncates a long tattoo description to 100 chars with an ellipsis`() {
        val longDescription = "x".repeat(120)
        val appointment = buildAppointment(
            description = longDescription,
            submittedAt = Instant.parse("2026-09-09T10:00:00Z")
        )

        emailService.sendAppointmentLinksEmail(listOf(appointment))

        verify(resendEmailClient).sendEmail(
            "client@test.com",
            "retrieve-appointment-links",
            mapOf(
                "ARTIST_NAME" to "Test Artist",
                "CLIENT_FIRSTNAME" to "Jane",
                "APPOINTMENTS_HTML" to (
                    "<ul>" +
                        "<li><a href=\"http://localhost:8080/appointment/${appointment.id}\">" +
                        "09/09/26 &ndash; &laquo; ${"x".repeat(100)}... &raquo;</a></li>" +
                        "</ul>"
                    )
            )
        )
    }

    @Test
    fun `sendSupportMessageConfirmationEmail delegates to client`() {
        emailService.sendSupportMessageConfirmationEmail("artist@test.com", "Test Artist")

        verify(resendEmailClient).sendEmail(
            "artist@test.com",
            "confirm-artist-new-support-ticket",
            mapOf("ARTIST_NAME" to "Test Artist")
        )
    }

    private fun buildArtist(email: String = "artist@test.com") = TattooArtist(
        id = UUID.randomUUID(),
        email = email,
        password = "hashed",
        artistName = "Test Artist",
        slug = "test-artist",
        city = "Test City",
        countryCode = "FR",
        registeredAt = Instant.now(),
        activatedAt = Instant.now()
    )

    private fun buildAppointment(
        clientEmail: String = "client@test.com",
        artistEmail: String = "artist@test.com",
        artist: TattooArtist = buildArtist(artistEmail),
        client: TattooClient = TattooClient(email = clientEmail, firstName = "Jane", lastName = "Doe"),
        description: String = "A beautiful dragon tattoo",
        submittedAt: Instant = Instant.now()
    ): Appointment = Appointment(
        artist = artist,
        client = client,
        tattooDescription = description,
        tattooPlacement = "forearm",
        tattooSize = "10x10cm",
        firstTattoo = false,
        coverUp = false,
        color = false,
        style = TattooStyle.REALISM,
        submittedAt = submittedAt
    )
}
