package com.inkvite.inkviteback.appointment.dto

import com.inkvite.inkviteback.appointment.entity.Appointment
import com.inkvite.inkviteback.appointment.entity.TattooStyle
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

data class ClientAppointmentDetailsResponseDto(
    val id: UUID,
    val tattooDescription: String,
    val tattooPlacement: String,
    val tattooSize: String,
    val firstTattoo: Boolean,
    val coverUp: Boolean,
    val color: Boolean,
    val style: TattooStyle,
    val receivedAt: LocalDate,
    val references: List<ReferenceDetailsResponseDto>,
    val artistName: String,
    val unreadMessages: Boolean
) {
    constructor(
        appointment: Appointment,
        references: List<ReferenceDetailsResponseDto>,
        unreadMessages: Boolean
    ) : this(
        id = appointment.id,
        tattooDescription = appointment.tattooDescription,
        tattooPlacement = appointment.tattooPlacement,
        tattooSize = appointment.tattooSize,
        firstTattoo = appointment.firstTattoo,
        coverUp = appointment.coverUp,
        color = appointment.color,
        style = appointment.style,
        receivedAt = LocalDate.ofInstant(appointment.verifiedAt, ZoneId.of("UTC")),
        references = references,
        artistName = appointment.artist.artistName,
        unreadMessages = unreadMessages
    )
}
