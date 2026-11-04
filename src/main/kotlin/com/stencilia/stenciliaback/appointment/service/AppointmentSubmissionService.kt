package com.stencilia.stenciliaback.appointment.service

import com.stencilia.stenciliaback.appointment.dto.AppointmentFormRequestDto
import com.stencilia.stenciliaback.storage.dto.ImageUploadResponseDto
import org.springframework.web.multipart.MultipartFile
import java.util.*

interface AppointmentSubmissionService {
    fun save(appointmentDto: AppointmentFormRequestDto, slug: String)
    fun uploadReference(slug: String, photo: MultipartFile): ImageUploadResponseDto
    fun verify(appointmentId: UUID)
    fun requestLinks(slug: String, email: String)
}
