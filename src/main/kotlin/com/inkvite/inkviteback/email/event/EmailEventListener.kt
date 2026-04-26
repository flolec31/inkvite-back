package com.inkvite.inkviteback.email.event

import com.inkvite.inkviteback.appointment.event.AppointmentNotificationEmailRequested
import com.inkvite.inkviteback.appointment.event.AppointmentVerificationEmailRequested
import com.inkvite.inkviteback.auth.event.ArtistVerificationEmailRequested
import com.inkvite.inkviteback.email.service.EmailService
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class EmailEventListener(
    private val emailService: EmailService
) {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun on(event: ArtistVerificationEmailRequested) {
        emailService.sendArtistVerificationEmail(event.to, event.token)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun on(event: AppointmentVerificationEmailRequested) {
        emailService.sendAppointmentVerificationEmail(event.appointment)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun on(event: AppointmentNotificationEmailRequested) {
        emailService.sendAppointmentNotificationEmail(event.appointment)
    }
}