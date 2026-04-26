package com.inkvite.inkviteback.appointment.dto

import com.inkvite.inkviteback.appointment.entity.Appointment
import com.inkvite.inkviteback.appointment.entity.Reference
import jakarta.validation.constraints.NotBlank

data class ReferenceRequestDto(
    @field:NotBlank val key: String,
    val comment: String? = null,
) {
    fun toEntity(appointment: Appointment): Reference =
        Reference(
            key = key,
            comment = comment,
            appointment = appointment
        )
}