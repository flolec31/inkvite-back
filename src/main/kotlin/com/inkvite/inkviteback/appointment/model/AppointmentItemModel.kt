package com.inkvite.inkviteback.appointment.model

import com.inkvite.inkviteback.appointment.dto.AppointmentItemResponseDto
import java.time.LocalDate
import java.util.UUID

data class AppointmentItemModel(
    val id: UUID,
    val description: String,
    val firstName: String,
    val lastName: String,
    val tattooPlacement: String,
    val receivedAt: LocalDate,
    val new: Boolean
) {
    fun toDto(): AppointmentItemResponseDto = AppointmentItemResponseDto(
        id = id,
        description = description,
        firstName = firstName,
        lastName = lastName,
        tattooPlacement = tattooPlacement,
        receivedAt = receivedAt,
        new = new
    )
}