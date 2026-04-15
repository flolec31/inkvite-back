package com.inkvite.inkviteback.email.event

import com.inkvite.inkviteback.auth.event.VerificationEmailRequested
import com.inkvite.inkviteback.email.service.EmailService
import org.springframework.stereotype.Component
import org.springframework.context.event.EventListener

@Component
class EmailEventListener(
    private val emailService: EmailService
) {

    @EventListener
    fun on(event: VerificationEmailRequested) {
        emailService.sendVerificationEmail(event.to, event.token)
    }
}