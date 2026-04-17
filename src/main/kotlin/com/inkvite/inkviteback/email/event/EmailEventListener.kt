package com.inkvite.inkviteback.email.event

import com.inkvite.inkviteback.auth.event.VerificationEmailRequested
import com.inkvite.inkviteback.email.service.EmailService
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class EmailEventListener(
    private val emailService: EmailService
) {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun on(event: VerificationEmailRequested) {
        emailService.sendVerificationEmail(event.to, event.token)
    }
}