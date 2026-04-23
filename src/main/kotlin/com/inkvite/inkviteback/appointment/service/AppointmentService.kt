package com.inkvite.inkviteback.appointment.service

import com.inkvite.inkviteback.appointment.model.AppointmentFormModel
import com.inkvite.inkviteback.appointment.model.AppointmentItemModel
import com.inkvite.inkviteback.appointment.model.UploadedReferenceModel
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.multipart.MultipartFile
import java.util.*

interface AppointmentService {
    fun save(appointmentFormModel: AppointmentFormModel)
    fun uploadReference(slug: String, photo: MultipartFile): UploadedReferenceModel
    fun verify(formId: UUID)
    fun getAppointmentsOf(artistId: UUID, pageable: Pageable): Page<AppointmentItemModel>
}