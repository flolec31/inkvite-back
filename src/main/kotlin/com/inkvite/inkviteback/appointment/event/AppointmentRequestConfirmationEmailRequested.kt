package com.inkvite.inkviteback.appointment.event

import com.inkvite.inkviteback.appointment.entity.Appointment

data class AppointmentRequestConfirmationEmailRequested(val appointment: Appointment)
