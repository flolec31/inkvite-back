package com.inkvite.inkviteback.support.entity

import com.inkvite.inkviteback.artist.entity.TattooArtist
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "support_message")
class SupportMessage(
    @Id var id: UUID = UUID.randomUUID(),
    @ManyToOne @JoinColumn(name = "artist_id", nullable = false)
    var artist: TattooArtist,
    @Enumerated(EnumType.STRING)
    var type: SupportMessageType,
    var message: String,
    var screenshotKey: String? = null,
    var createdAt: Instant,
)
