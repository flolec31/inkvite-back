package com.inkvite.inkviteback.artist.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "tattoo_artist")
class TattooArtist(
    @Id var id: UUID,
    var email: String,
    var password: String,
    var artistName: String,
    var slug: String,
    var registeredAt: Instant,
    var activatedAt: Instant? = null,
    var profilePhotoKey: String? = null,
)