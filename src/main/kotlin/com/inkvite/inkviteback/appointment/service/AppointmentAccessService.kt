package com.inkvite.inkviteback.appointment.service

import com.inkvite.inkviteback.appointment.entity.Appointment
import java.util.UUID

interface AppointmentAccessService {
    fun findOwnedAppointment(artistId: UUID, appointmentId: UUID): Appointment
}
