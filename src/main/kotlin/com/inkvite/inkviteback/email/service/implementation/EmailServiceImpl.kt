package com.inkvite.inkviteback.email.service.implementation

import com.inkvite.inkviteback.appointment.entity.Appointment
import com.inkvite.inkviteback.email.client.ResendEmailClient
import com.inkvite.inkviteback.email.service.EmailService
import com.inkvite.inkviteback.support.entity.SupportMessage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Service
class EmailServiceImpl(
    private val resendEmailClient: ResendEmailClient,
    @Value($$"${app.base-url}") private val baseUrl: String,
    @Value($$"${app.support.notification-email}") private val supportNotificationEmail: String,
) : EmailService {

    private val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val MAX_DESCRIPTION_LENGTH = 100
        private val LINKS_DATE_FORMATTER: DateTimeFormatter =
            DateTimeFormatter.ofPattern("dd/MM/yy").withZone(ZoneOffset.UTC)
    }

    override fun sendArtistVerificationEmail(to: String, artistName: String, token: String) {
        logger.debug("Sending artist verification email to: $to")
        val link = UriComponentsBuilder.fromUriString(baseUrl)
            .path("/sign-up/verify")
            .queryParam("token", token)
            .toUriString()
        val variables = mapOf(
            "LINK" to link,
            "ARTIST_NAME" to artistName
        )
        resendEmailClient.sendEmail(to, "verify-artist-signup", variables)
    }

    override fun sendPasswordResetEmail(to: String, artistName: String, token: String) {
        logger.debug("Sending password reset email to: $to")
        val link = UriComponentsBuilder.fromUriString(baseUrl)
            .path("/reset-password")
            .queryParam("token", token)
            .toUriString()
        val variables = mapOf(
            "LINK" to link,
            "ARTIST_NAME" to artistName
        )
        resendEmailClient.sendEmail(to, "verify-reset-password-3", variables)
    }

    override fun sendPasswordChangedEmail(to: String, artistName: String) {
        logger.debug("Sending password changed email to: $to")
        val variables = mapOf("ARTIST_NAME" to artistName)
        resendEmailClient.sendEmail(to, "confirm-password-change", variables)
    }

    override fun sendAppointmentVerificationEmail(appointment: Appointment) {
        val to = appointment.client.email
        logger.debug("Sending appointment verification email to: $to")
        val link = UriComponentsBuilder.fromUriString(baseUrl)
            .path("/@${appointment.artist.slug}/verify")
            .queryParam("appointmentId", appointment.id)
            .toUriString()
        val variables = mapOf(
            "LINK" to link,
            "ARTIST_NAME" to appointment.artist.artistName,
            "CLIENT_FIRSTNAME" to appointment.client.firstName
        )
        resendEmailClient.sendEmail(to, "verify-appointment-request", variables)
    }

    override fun sendAppointmentNotificationEmail(appointment: Appointment) {
        val to = appointment.artist.email
        logger.debug("Sending appointment notification email to: $to")
        val variables = mapOf(
            "LINK" to artistDashboardLink(),
            "ARTIST_NAME" to appointment.artist.artistName,
            "CLIENT_NAME" to appointment.client.getFullName()
        )
        resendEmailClient.sendEmail(to, "notify-artist-new-appointment-request", variables)
    }

    override fun sendNewMessageEmailToClient(appointment: Appointment) {
        val to = appointment.client.email
        logger.debug("Sending new message notification email to client: $to")
        val variables = mapOf(
            "LINK" to clientAppointmentLink(appointment),
            "ARTIST_NAME" to appointment.artist.artistName,
            "CLIENT_FIRSTNAME" to appointment.client.firstName
        )
        resendEmailClient.sendEmail(to, "notify-client-new-message", variables)
    }

    override fun sendNewMessageEmailToArtist(appointment: Appointment) {
        val to = appointment.artist.email
        logger.debug("Sending new message notification email to artist: $to")
        val variables = mapOf(
            "LINK" to artistDashboardLink(),
            "ARTIST_NAME" to appointment.artist.artistName,
            "CLIENT_NAME" to appointment.client.getFullName()
        )
        resendEmailClient.sendEmail(to, "notify-artist-new-message", variables)
    }

    override fun sendAppointmentRequestConfirmationEmail(appointment: Appointment) {
        val to = appointment.client.email
        logger.debug("Sending appointment confirmation email to: $to")
        val variables = mapOf(
            "LINK" to clientAppointmentLink(appointment),
            "ARTIST_NAME" to appointment.artist.artistName,
            "CLIENT_FIRSTNAME" to appointment.client.firstName
        )
        resendEmailClient.sendEmail(to, "confirm-client-appointment-request", variables)
    }

    override fun sendAppointmentLinksEmail(appointments: List<Appointment>) {
        val client = appointments.first().client
        logger.debug("Sending appointment links email to: ${client.email}")
        // Resend templates only substitute string/number values; they cannot loop over a list.
        // So the variable-length appointment list is pre-rendered here into a single HTML string,
        // injected via a triple-brace {{{APPOINTMENTS_HTML}}} variable (rendered as raw HTML).
        // The tattoo description is client-supplied, so it must be HTML-escaped.
        val appointmentsHtml = appointments.joinToString(separator = "", prefix = "<ul>", postfix = "</ul>") { appointment ->
            val link = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/appointment/${appointment.id}")
                .toUriString()
            val date = LINKS_DATE_FORMATTER.format(appointment.submittedAt)
            val description = escapeHtml(truncate(appointment.tattooDescription))
            "<li><a href=\"$link\">$date &ndash; &laquo; $description &raquo;</a></li>"
        }
        val variables = mapOf(
            "ARTIST_NAME" to appointments.first().artist.artistName,
            "CLIENT_FIRSTNAME" to client.firstName,
            "APPOINTMENTS_HTML" to appointmentsHtml
        )
        resendEmailClient.sendEmail(client.email, "retrieve-appointment-links", variables)
    }

    /** The client-facing link to a single appointment's page (also carries its message thread). */
    private fun clientAppointmentLink(appointment: Appointment): String =
        UriComponentsBuilder.fromUriString(baseUrl)
            .path("/appointment/${appointment.id}")
            .toUriString()

    /** The artist's dashboard link (no per-appointment deep link exists yet). */
    private fun artistDashboardLink(): String =
        UriComponentsBuilder.fromUriString(baseUrl)
            .path("/dashboard")
            .toUriString()

    private fun truncate(value: String): String =
        if (value.length > MAX_DESCRIPTION_LENGTH) value.take(MAX_DESCRIPTION_LENGTH).trimEnd() + "..." else value

    private fun escapeHtml(value: String): String = value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")

    override fun sendSupportMessageReceivedEmail(supportMessage: SupportMessage) {
        logger.debug("Sending support message notification email to: $supportNotificationEmail")
        val variables = mapOf(
            "TYPE" to supportMessage.type.name,
            "ARTIST_NAME" to supportMessage.artist.artistName,
            "ARTIST_EMAIL" to supportMessage.artist.email,
            "MESSAGE" to supportMessage.message
        )
        resendEmailClient.sendEmail(supportNotificationEmail, "new-support-message", variables)
    }

    override fun sendSupportMessageConfirmationEmail(to: String, artistName: String) {
        logger.debug("Sending support message confirmation email to: $to")
        val variables = mapOf("ARTIST_NAME" to artistName)
        resendEmailClient.sendEmail(to, "confirm-artist-new-support-ticket", variables)
    }

    override fun sendClientAccessCodeEmail(to: String, clientFirstName: String, code: String) {
        logger.debug("Sending client access code email to: $to")
        val variables = mapOf(
            "CLIENT_FIRSTNAME" to clientFirstName,
            "CODE" to code,
        )
        resendEmailClient.sendEmail(to, "verify-client-access-code", variables)
    }

}