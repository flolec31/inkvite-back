package com.inkvite.inkviteback.appointment.dto

import com.inkvite.inkviteback.appointment.entity.Appointment
import com.inkvite.inkviteback.appointment.entity.TattooStyle
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

data class AppointmentItemResponseDto(
    val id: UUID,
    val description: String,
    val firstName: String,
    val lastName: String,
    val tattooPlacement: String,
    val style: TattooStyle,
    val receivedAt: LocalDate,
    val new: Boolean,
    val archived: Boolean
) {
    constructor(appointment: Appointment) : this(
        id = appointment.id,
        description = appointment.tattooDescription,
        firstName = appointment.client.firstName,
        lastName = appointment.client.lastName,
        tattooPlacement = appointment.tattooPlacement,
        style = appointment.style,
        receivedAt = LocalDate.ofInstant(appointment.verifiedAt, ZoneId.of("UTC")),
        new = appointment.new,
        archived = appointment.archived
    )
}
