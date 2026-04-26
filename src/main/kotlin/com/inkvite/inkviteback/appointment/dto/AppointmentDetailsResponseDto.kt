package com.inkvite.inkviteback.appointment.dto

import com.inkvite.inkviteback.appointment.entity.Appointment
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

data class AppointmentDetailsResponseDto(
    val id: UUID,
    val tattooDescription: String,
    val tattooPlacement: String,
    val tattooSize: String,
    val firstTattoo: Boolean,
    val coverUp: Boolean,
    val receivedAt: LocalDate,
    val references: List<ReferenceDetailsResponseDto>,
    val clientName: String,
    val clientEmail: String
) {
    constructor(
        appointment: Appointment,
        references: List<ReferenceDetailsResponseDto>
    ) : this(
        id = appointment.id,
        tattooDescription = appointment.tattooDescription,
        tattooPlacement = appointment.tattooPlacement,
        tattooSize = appointment.tattooSize,
        firstTattoo = appointment.firstTattoo,
        coverUp = appointment.coverUp,
        receivedAt = LocalDate.ofInstant(appointment.verifiedAt, ZoneId.of("UTC")),
        references = references,
        clientName = appointment.client.getFullName(),
        clientEmail = appointment.client.email
    )
}