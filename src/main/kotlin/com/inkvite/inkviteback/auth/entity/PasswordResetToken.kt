package com.inkvite.inkviteback.auth.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@Entity
@Table(name = "password_reset_token")
class PasswordResetToken(
    @Id var token: String = UUID.randomUUID().toString(),
    var tattooArtistId: UUID,
    var expiresAt: Instant = Instant.now().plus(1, ChronoUnit.HOURS),
)
