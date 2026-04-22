package com.inkvite.inkviteback.appointment.model

import com.inkvite.inkviteback.appointment.entity.AppointmentForm
import com.inkvite.inkviteback.appointment.entity.Reference

data class CommentedReferenceModel(
    val key: String,
    val comment: String?
) {
    fun toEntity(appointmentForm: AppointmentForm): Reference =
        Reference(
            key = key,
            comment = comment,
            appointmentForm = appointmentForm
        )
}
