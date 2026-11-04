package com.stencilia.stenciliaback.appointment.dto

import com.stencilia.stenciliaback.appointment.entity.Appointment
import com.stencilia.stenciliaback.appointment.entity.Reference
import jakarta.validation.constraints.NotBlank

data class ReferenceRequestDto(
    @field:NotBlank val key: String,
    val comment: String? = null,
) {
    fun toEntity(appointment: Appointment): Reference =
        Reference(
            key = key,
            comment = comment,
            appointment = appointment,
        )
}