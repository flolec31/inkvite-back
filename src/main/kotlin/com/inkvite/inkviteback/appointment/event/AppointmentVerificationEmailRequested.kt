package com.inkvite.inkviteback.appointment.event

import java.util.UUID

data class AppointmentVerificationEmailRequested (
    val to: String,
    val form: UUID
)