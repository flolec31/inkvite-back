package com.inkvite.inkviteback.artist

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "tattoo_artist")
class TattooArtist(
    @Id var id: UUID,
    var email: String,
    var passwordHash: String,
    var active: Boolean = false,
)