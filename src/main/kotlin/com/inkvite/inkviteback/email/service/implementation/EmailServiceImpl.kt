package com.inkvite.inkviteback.email.service.implementation

import com.inkvite.inkviteback.email.client.ResendEmailClient
import com.inkvite.inkviteback.email.service.EmailService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder

@Service
class EmailServiceImpl(
    private val resendEmailClient: ResendEmailClient,
    @Value($$"${app.base-url}") private val baseUrl: String
) : EmailService {

    override fun sendVerificationEmail(to: String, token: String) {
        val link = UriComponentsBuilder.fromUriString(baseUrl)
            .path("/auth/verify")
            .queryParam("token", token)
            .toUriString()
        val variables = mapOf("link" to link)
        resendEmailClient.sendEmail(to, "verify-email", variables)
    }


}