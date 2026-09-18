package com.inkvite.inkviteback.appointment.service

import com.inkvite.inkviteback.appointment.dto.ClientAppointmentDetailsResponseDto
import java.util.*

fun interface AppointmentClientService {
    fun getAppointmentDetails(clientId: UUID, appointmentId: UUID): ClientAppointmentDetailsResponseDto
}
