package com.stencilia.stenciliaback.appointment.event

import com.stencilia.stenciliaback.appointment.entity.Appointment

data class AppointmentNotificationEmailRequested(val appointment: Appointment)