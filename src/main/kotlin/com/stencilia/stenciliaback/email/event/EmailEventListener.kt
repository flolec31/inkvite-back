package com.stencilia.stenciliaback.email.event

import com.stencilia.stenciliaback.appointment.event.AppointmentRequestConfirmationEmailRequested
import com.stencilia.stenciliaback.appointment.event.AppointmentLinksRequested
import com.stencilia.stenciliaback.appointment.event.AppointmentNotificationEmailRequested
import com.stencilia.stenciliaback.appointment.event.AppointmentVerificationEmailRequested
import com.stencilia.stenciliaback.auth.artist.event.ArtistVerificationEmailRequested
import com.stencilia.stenciliaback.auth.artist.event.PasswordChangedEmailRequested
import com.stencilia.stenciliaback.auth.artist.event.PasswordResetEmailRequested
import com.stencilia.stenciliaback.auth.client.event.ClientAccessCodeRequested
import com.stencilia.stenciliaback.discussion.event.NewMessageEmailRequested
import com.stencilia.stenciliaback.discussion.event.NewMessageToArtistEmailRequested
import com.stencilia.stenciliaback.email.service.EmailService
import com.stencilia.stenciliaback.support.event.SupportMessageConfirmationEmailRequested
import com.stencilia.stenciliaback.support.event.SupportMessageReceivedEmailRequested
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class EmailEventListener(
    private val emailService: EmailService
) {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun on(event: ArtistVerificationEmailRequested) {
        emailService.sendArtistVerificationEmail(event.to, event.artistName, event.token)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun on(event: PasswordResetEmailRequested) {
        emailService.sendPasswordResetEmail(event.to, event.artistName, event.token)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun on(event: PasswordChangedEmailRequested) {
        emailService.sendPasswordChangedEmail(event.to, event.artistName)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun on(event: AppointmentVerificationEmailRequested) {
        emailService.sendAppointmentVerificationEmail(event.appointment)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun on(event: AppointmentNotificationEmailRequested) {
        emailService.sendAppointmentNotificationEmail(event.appointment)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun on(event: AppointmentRequestConfirmationEmailRequested) {
        emailService.sendAppointmentRequestConfirmationEmail(event.appointment)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun on(event: AppointmentLinksRequested) {
        emailService.sendAppointmentLinksEmail(event.appointments)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun on(event: NewMessageEmailRequested) {
        emailService.sendNewMessageEmailToClient(event.appointment)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun on(event: NewMessageToArtistEmailRequested) {
        emailService.sendNewMessageEmailToArtist(event.appointment)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun on(event: SupportMessageReceivedEmailRequested) {
        emailService.sendSupportMessageReceivedEmail(event.supportMessage)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun on(event: SupportMessageConfirmationEmailRequested) {
        emailService.sendSupportMessageConfirmationEmail(event.to, event.artistName)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun on(event: ClientAccessCodeRequested) {
        emailService.sendClientAccessCodeEmail(event.to, event.clientFirstName, event.code)
    }
}
