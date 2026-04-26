package com.inkvite.inkviteback.appointment.dto

import com.inkvite.inkviteback.appointment.entity.Appointment
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

data class AppointmentItemResponseDto(
    val id: UUID,
    val description: String,
    val firstName: String,
    val lastName: String,
    val tattooPlacement: String,
    val receivedAt: LocalDate,
    val new: Boolean
) {
    constructor(appointment: Appointment) : this(
        id = appointment.id,
        description = appointment.tattooDescription,
        firstName = appointment.client.firstName,
        lastName = appointment.client.lastName,
        tattooPlacement = appointment.tattooPlacement,
        receivedAt = LocalDate.ofInstant(appointment.verifiedAt, ZoneId.of("UTC")),
        new = appointment.new
    )
}