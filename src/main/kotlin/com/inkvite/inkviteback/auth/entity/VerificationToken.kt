package com.inkvite.inkviteback.auth.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "verification_token")
class VerificationToken(
    @Id var token: String,
    var tattooArtistId: UUID,
    var expiresAt: Instant,
)