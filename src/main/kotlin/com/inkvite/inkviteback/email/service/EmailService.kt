package com.inkvite.inkviteback.email.service

import com.inkvite.inkviteback.appointment.entity.Appointment
import com.inkvite.inkviteback.support.entity.SupportMessage

interface EmailService {
    /** Sent to the artist right after sign-up, to verify their email before they can log in. */
    fun sendArtistVerificationEmail(to: String, artistName: String, token: String)

    /** Sent to the artist when they request a password reset ("forgot password"). */
    fun sendPasswordResetEmail(to: String, artistName: String, token: String)

    /** Sent to the artist whenever their password changes, whether via the authenticated change-password flow or a completed password reset. */
    fun sendPasswordChangedEmail(to: String, artistName: String)

    /** Sent to the client after they submit an appointment request, to verify their email before the artist is notified. */
    fun sendAppointmentVerificationEmail(appointment: Appointment)

    /** Sent to the artist once the client has verified their appointment request. */
    fun sendAppointmentNotificationEmail(appointment: Appointment)

    /** Sent to the client when the artist posts a new message in their appointment's discussion thread. */
    fun sendNewMessageEmailToClient(appointment: Appointment)

    /** Sent to the support notification address when an artist submits a support/contact message. */
    fun sendSupportMessageReceivedEmail(supportMessage: SupportMessage)

    /** Sent to the artist confirming their support message was received. */
    fun sendSupportMessageConfirmationEmail(to: String, artistName: String)
}