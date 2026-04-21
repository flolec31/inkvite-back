package com.inkvite.inkviteback.email.service.implementation

import com.inkvite.inkviteback.email.client.ResendEmailClient
import com.inkvite.inkviteback.email.service.EmailService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import java.util.UUID

@Service
class EmailServiceImpl(
    private val resendEmailClient: ResendEmailClient,
    @Value($$"${app.base-url}") private val baseUrl: String
) : EmailService {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun sendArtistVerificationEmail(to: String, token: String) {
        val link = UriComponentsBuilder.fromUriString(baseUrl)
            .path("/auth/verify")
            .queryParam("token", token)
            .toUriString()
        val variables = mapOf("link" to link)
        logger.debug("Sending artist verification email to: $to")
        resendEmailClient.sendEmail(to, "verify-artist-email", variables)
    }

    override fun sendAppointmentVerificationEmail(to: String, formId: UUID) {
        val link = UriComponentsBuilder.fromUriString(baseUrl)
            .path("/appointment/verify")
            .queryParam("formId", formId)
            .toUriString()
        val variables = mapOf("link" to link)
        logger.debug("Sending appointment verification email to: $to")
        resendEmailClient.sendEmail(to, "verify-appointment-email", variables)
    }

}