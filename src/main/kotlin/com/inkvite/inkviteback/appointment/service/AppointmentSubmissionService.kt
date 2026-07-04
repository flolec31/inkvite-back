package com.inkvite.inkviteback.appointment.service

import com.inkvite.inkviteback.appointment.dto.AppointmentFormRequestDto
import com.inkvite.inkviteback.appointment.dto.ReferenceUploadResponseDto
import org.springframework.web.multipart.MultipartFile
import java.util.*

interface AppointmentSubmissionService {
    fun save(appointmentDto: AppointmentFormRequestDto, slug: String)
    fun uploadReference(slug: String, photo: MultipartFile): ReferenceUploadResponseDto
    fun verify(appointmentId: UUID)
}
