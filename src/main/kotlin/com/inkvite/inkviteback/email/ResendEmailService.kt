package com.inkvite.inkviteback.email

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

@Service
class ResendEmailService(
    builder: RestClient.Builder,
    @Value("\${resend.api-key}") private val apiKey: String,
    @Value("\${app.base-url}") private val baseUrl: String,
) : EmailService {

    private val log = LoggerFactory.getLogger(javaClass)
    private val restClient = builder.baseUrl("https://api.resend.com").build()

    override fun sendVerificationEmail(to: String, token: String) {
        restClient.post()
            .uri("/emails")
            .header("Authorization", "Bearer $apiKey")
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                mapOf(
                    "from" to "noreply@inkvite.com",
                    "to" to listOf(to),
                    "subject" to "Verify your Inkvite email",
                    "html" to "<p>Click <a href='$baseUrl/auth/verify?token=$token'>here</a> to verify your email.</p>",
                )
            )
            .retrieve()
            .onStatus(HttpStatusCode::isError) { _, response ->
                log.error("Failed to send verification email to {}: {} {}", to, response.statusCode, response.statusText)
                throw EmailDeliveryException("Failed to send verification email to $to: ${response.statusCode}")
            }
            .toBodilessEntity()
    }
}

class EmailDeliveryException(message: String) : RuntimeException(message)
