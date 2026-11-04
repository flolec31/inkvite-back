package com.stencilia.stenciliaback.appointment.service

import com.stencilia.stenciliaback.appointment.dto.ClientAppointmentDetailsResponseDto
import java.util.*

fun interface AppointmentClientService {
    fun getAppointmentDetails(clientId: UUID, appointmentId: UUID): ClientAppointmentDetailsResponseDto
}
