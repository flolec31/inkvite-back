package com.inkvite.inkviteback.appointment.service

import com.inkvite.inkviteback.appointment.model.AppointmentFormModel
import java.util.UUID

interface AppointmentService {
    fun save(appointmentFormModel: AppointmentFormModel)
    fun verify(formId: UUID)
}