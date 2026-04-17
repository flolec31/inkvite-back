package com.inkvite.inkviteback.email.client

import com.inkvite.inkviteback.email.exception.EmailDeliveryException
import com.resend.Resend
import com.resend.services.emails.model.CreateEmailOptions
import com.resend.services.emails.model.Template
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class ResendEmailClient(
    private val resend: Resend,
    @Value($$"${app.email.from}") private val from: String,
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun sendEmail(to: String, templateId: String, variables: Map<String, String>) {
        val template = Template.builder()
            .id(templateId)
            .variables(variables)
            .build()

        val request = CreateEmailOptions.builder()
            .from(from)
            .to(to)
            .template(template)
            .build()

        try {
            resend.emails().send(request)
        } catch (e: Exception) {
            logger.error("Failed to send verification email to {}", to, e)
            throw EmailDeliveryException("Failed to send verification email to $to: ${e.message}")
        }
    }
}