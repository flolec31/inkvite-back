package com.inkvite.inkviteback.appointment.event

import com.inkvite.inkviteback.appointment.entity.Appointment

data class AppointmentNotificationEmailRequested(val appointment: Appointment)