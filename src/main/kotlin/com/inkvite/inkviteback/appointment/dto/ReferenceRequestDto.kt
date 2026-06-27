package com.inkvite.inkviteback.appointment.dto

import com.inkvite.inkviteback.appointment.entity.Appointment
import com.inkvite.inkviteback.appointment.entity.Reference
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero

data class ReferenceRequestDto(
    @field:NotBlank val key: String,
    val comment: String? = null,
    @field:Valid val crop: CropCoordinatesDto
) {
    fun toEntity(appointment: Appointment): Reference =
        Reference(
            key = key,
            comment = comment,
            appointment = appointment,
            cropLeft = crop.left,
            cropTop = crop.top,
            cropWidth = crop.width,
            cropHeight = crop.height,
        )
}

data class CropCoordinatesDto(
    @field:PositiveOrZero val left: Int,
    @field:PositiveOrZero val top: Int,
    @field:PositiveOrZero val width: Int,
    @field:PositiveOrZero val height: Int,
)