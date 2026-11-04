package com.stencilia.stenciliaback.appointment.event

import com.stencilia.stenciliaback.appointment.entity.Appointment

data class AppointmentRequestConfirmationEmailRequested(val appointment: Appointment)
