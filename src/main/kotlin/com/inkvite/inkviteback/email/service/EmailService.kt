package com.inkvite.inkviteback.email.service

import java.util.UUID

interface EmailService {
    fun sendArtistVerificationEmail(to: String, token: String)
    fun sendAppointmentVerificationEmail(to: String, formId: UUID)
}