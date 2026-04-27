package com.inkvite.inkviteback.email.service

import com.inkvite.inkviteback.appointment.entity.Appointment

interface EmailService {
    fun sendArtistVerificationEmail(to: String, artistName: String, token: String)
    fun sendPasswordResetEmail(to: String, artistName: String, token: String)
    fun sendAppointmentVerificationEmail(appointment: Appointment)
    fun sendAppointmentNotificationEmail(appointment: Appointment)
}