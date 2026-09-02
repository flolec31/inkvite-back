package com.inkvite.inkviteback.email.service.implementation

import com.inkvite.inkviteback.appointment.entity.Appointment
import com.inkvite.inkviteback.email.client.ResendEmailClient
import com.inkvite.inkviteback.email.service.EmailService
import com.inkvite.inkviteback.support.entity.SupportMessage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder

@Service
class EmailServiceImpl(
    private val resendEmailClient: ResendEmailClient,
    @Value($$"${app.base-url}") private val baseUrl: String,
    @Value($$"${app.support.notification-email}") private val supportNotificationEmail: String,
) : EmailService {

    private val logger = LoggerFactory.getLogger(javaClass)

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
        val link = UriComponentsBuilder.fromUriString(baseUrl)
            .path("/dashboard")
            .toUriString()
        val variables = mapOf(
            "LINK" to link,
            "ARTIST_NAME" to appointment.artist.artistName,
            "CLIENT_NAME" to appointment.client.getFullName()
        )
        resendEmailClient.sendEmail(to, "notify-artist-new-appointment-request", variables)
    }

    override fun sendNewMessageEmailToClient(appointment: Appointment) {
        val to = appointment.client.email
        logger.debug("Sending new message notification email to: $to")
        val variables = mapOf(
            "ARTIST_NAME" to appointment.artist.artistName,
            "CLIENT_FIRSTNAME" to appointment.client.firstName
        )
        resendEmailClient.sendEmail(to, "notify-client-new-message", variables)
    }

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

}