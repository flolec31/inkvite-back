package com.stencilia.stenciliaback.appointment.service

import com.stencilia.stenciliaback.appointment.dto.AppointmentDetailsResponseDto
import com.stencilia.stenciliaback.appointment.dto.AppointmentItemResponseDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.*

interface AppointmentManagementService {
    fun getAppointmentsOf(artistId: UUID, pageable: Pageable): Page<AppointmentItemResponseDto>
    fun getAppointmentDetails(artistId: UUID, appointmentId: UUID): AppointmentDetailsResponseDto
    fun archiveAppointment(artistId: UUID, appointmentId: UUID)
    fun unarchiveAppointment(artistId: UUID, appointmentId: UUID)
    fun markAppointmentAsNew(artistId: UUID, appointmentId: UUID)
}
