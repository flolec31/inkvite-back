package com.inkvite.inkviteback.appointment.service

import com.inkvite.inkviteback.appointment.dto.AppointmentDetailsResponseDto
import com.inkvite.inkviteback.appointment.dto.AppointmentFormRequestDto
import com.inkvite.inkviteback.appointment.dto.AppointmentItemResponseDto
import com.inkvite.inkviteback.appointment.dto.ReferenceUploadResponseDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.multipart.MultipartFile
import java.util.*

interface AppointmentService {
    fun save(appointmentDto: AppointmentFormRequestDto, slug: String)
    fun uploadReference(slug: String, photo: MultipartFile): ReferenceUploadResponseDto
    fun verify(formId: UUID)
    fun getAppointmentsOf(artistId: UUID, pageable: Pageable): Page<AppointmentItemResponseDto>
    fun getAppointmentDetails(artistId: UUID, appointmentId: UUID): AppointmentDetailsResponseDto
}