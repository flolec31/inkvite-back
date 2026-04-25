package com.inkvite.inkviteback.appointment.dto

import java.time.LocalDate
import java.util.*

data class AppointmentItemResponseDto(
    val id: UUID,
    val description: String,
    val firstName: String,
    val lastName: String,
    val tattooPlacement: String,
    val receivedAt: LocalDate,
    val new: Boolean
)