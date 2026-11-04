package com.stencilia.stenciliaback.appointment.service

import com.stencilia.stenciliaback.appointment.entity.Appointment
import java.util.UUID

interface AppointmentAccessService {
    fun findOwnedAppointment(artistId: UUID, appointmentId: UUID): Appointment
    fun findAppointmentForClient(clientId: UUID, appointmentId: UUID): Appointment
}
