package com.inkvite.inkviteback.appointment.event

import com.inkvite.inkviteback.appointment.entity.Appointment

data class AppointmentLinksRequested(val appointments: List<Appointment>)
