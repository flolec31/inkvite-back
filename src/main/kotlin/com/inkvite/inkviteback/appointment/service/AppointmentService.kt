package com.inkvite.inkviteback.appointment.service

import com.inkvite.inkviteback.appointment.model.AppointmentFormModel
import com.inkvite.inkviteback.appointment.model.UploadedReferenceModel
import org.springframework.web.multipart.MultipartFile
import java.util.*

interface AppointmentService {
    fun save(appointmentFormModel: AppointmentFormModel)
    fun uploadReference(slug: String, photo: MultipartFile): UploadedReferenceModel
    fun verify(formId: UUID)
}