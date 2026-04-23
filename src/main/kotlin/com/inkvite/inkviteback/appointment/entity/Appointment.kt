package com.inkvite.inkviteback.appointment.entity

import com.inkvite.inkviteback.appointment.model.AppointmentFormModel
import com.inkvite.inkviteback.appointment.model.AppointmentItemModel
import com.inkvite.inkviteback.artist.entity.TattooArtist
import com.inkvite.inkviteback.client.entity.TattooClient
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

@Entity
@Table(name = "appointment")
class Appointment(
    @Id var id: UUID = UUID.randomUUID(),
    @ManyToOne @JoinColumn(name = "artist_id", nullable = false) var artist: TattooArtist,
    @ManyToOne @JoinColumn(name = "client_id", nullable = false) var client: TattooClient,
    var tattooDescription: String,
    var tattooPlacement: String,
    var tattooSize: String,
    var firstTattoo: Boolean,
    var coverUp: Boolean,
    var submittedAt: Instant = Instant.now(),
    var verifiedAt: Instant? = null,
    var new: Boolean = true
) {
    constructor(
        appointmentFormModel: AppointmentFormModel,
        artist: TattooArtist,
        client: TattooClient
    ) : this(
        artist = artist,
        client = client,
        tattooDescription = appointmentFormModel.tattooDescription,
        tattooPlacement = appointmentFormModel.tattooPlacement,
        tattooSize = appointmentFormModel.tattooSize,
        firstTattoo = appointmentFormModel.firstTattoo,
        coverUp = appointmentFormModel.coverUp
    )

    fun toModel(): AppointmentItemModel = AppointmentItemModel(
        id = id,
        description = tattooDescription,
        firstName = client.firstName,
        lastName = client.lastName,
        tattooPlacement = tattooPlacement,
        receivedAt = LocalDate.ofInstant(verifiedAt, ZoneId.of("UTC")),
        new = new
    )
}
