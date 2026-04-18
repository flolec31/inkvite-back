package com.inkvite.inkviteback.auth.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@Entity
@Table(name = "refresh_token")
class RefreshToken(
    @Id var token: UUID = UUID.randomUUID(),
    var tattooArtistId: UUID,
    var expiresAt: Instant = Instant.now().plus(30, ChronoUnit.DAYS),
)
