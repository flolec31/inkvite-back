package com.inkvite.inkviteback.email

import com.inkvite.inkviteback.auth.event.VerificationEmailRequested
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class EmailListener(private val emailService: EmailService) {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun on(event: VerificationEmailRequested) {
        emailService.sendVerificationEmail(event.to, event.token)
    }
}
