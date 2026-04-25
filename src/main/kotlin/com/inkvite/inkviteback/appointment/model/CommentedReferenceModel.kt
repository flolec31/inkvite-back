package com.inkvite.inkviteback.appointment.model

import com.inkvite.inkviteback.appointment.entity.Appointment
import com.inkvite.inkviteback.appointment.entity.Reference

data class CommentedReferenceModel(
    val key: String,
    val comment: String?
) {
    fun toEntity(appointment: Appointment): Reference =
        Reference(
            key = key,
            comment = comment,
            appointment = appointment
        )
}
