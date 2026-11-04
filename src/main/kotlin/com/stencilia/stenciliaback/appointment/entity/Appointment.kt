package com.stencilia.stenciliaback.appointment.entity

import com.stencilia.stenciliaback.artist.entity.TattooArtist
import com.stencilia.stenciliaback.client.entity.TattooClient
import jakarta.persistence.*
import java.time.Instant
import java.util.*

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
    var color: Boolean,
    @Enumerated(EnumType.STRING) var style: TattooStyle,
    var submittedAt: Instant = Instant.now(),
    var verifiedAt: Instant? = null,
    var new: Boolean = true,
    var archived: Boolean = false
)
