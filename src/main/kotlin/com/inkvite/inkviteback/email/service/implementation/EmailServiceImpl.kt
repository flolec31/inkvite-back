package com.inkvite.inkviteback.email.service.implementation

import com.inkvite.inkviteback.appointment.entity.Appointment
import com.inkvite.inkviteback.email.client.ResendEmailClient
import com.inkvite.inkviteback.email.service.EmailService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder

@Service
class EmailServiceImpl(
    private val resendEmailClient: ResendEmailClient,
    @Value($$"${app.base-url}") private val baseUrl: String
) : EmailService {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun sendArtistVerificationEmail(to: String, token: String) {
        logger.debug("Sending artist verification email to: $to")
        val link = UriComponentsBuilder.fromUriString(baseUrl)
            .path("/auth/verify")
            .queryParam("token", token)
            .toUriString()
        val variables = mapOf("LINK" to link)
        resendEmailClient.sendEmail(to, "verify-artist-email", variables)
    }

    override fun sendAppointmentVerificationEmail(appointment: Appointment) {
        val to = appointment.client.email
        logger.debug("Sending appointment verification email to: $to")
        val link = UriComponentsBuilder.fromUriString(baseUrl)
            .path("/appointment/verify")
            .queryParam("appointmentId", appointment.id)
            .toUriString()
        val variables = mapOf(
            "LINK" to link,
            "ARTIST_NAME" to appointment.artist.artistName,
            "CLIENT_FIRSTNAME" to appointment.client.firstName
        )
        resendEmailClient.sendEmail(to, "verify-appointment-email", variables)
    }

    override fun sendAppointmentNotificationEmail(appointment: Appointment) {
        val to = appointment.artist.email
        logger.debug("Sending appointment notification email to: $to")
        val link = UriComponentsBuilder.fromUriString(baseUrl)
            .path("/dashboard")
            .toUriString()
        val variables = mapOf(
            "DASHBOARD_LINK" to link,
            "ARTIST_NAME" to appointment.artist.artistName,
            "CLIENT_NAME" to appointment.client.getFullName()
        )
        resendEmailClient.sendEmail(to, "new-appointment-notification", variables)
    }

}