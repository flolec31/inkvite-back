package com.inkvite.inkviteback.auth.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@Entity
@Table(name = "verification_token")
class VerificationToken(
    @Id var token: String = UUID.randomUUID().toString(),
    var tattooArtistId: UUID,
    var expiresAt: Instant = Instant.now().plus(24, ChronoUnit.HOURS),
)