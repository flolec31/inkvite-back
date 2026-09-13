package com.inkvite.inkviteback.auth.client.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Version
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@Entity
@Table(name = "client_access_code")
class ClientAccessCode(
    @Id var clientId: UUID,
    var code: String,
    var attempts: Int = 0,
    var expiresAt: Instant = Instant.now().plus(10, ChronoUnit.MINUTES),
    var lastSentAt: Instant = Instant.now(),
    @Version var version: Long = 0,
)
