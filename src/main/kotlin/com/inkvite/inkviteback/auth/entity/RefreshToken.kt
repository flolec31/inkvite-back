package com.inkvite.inkviteback.auth.entity

import com.inkvite.inkviteback.auth.Role
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "refresh_token")
class RefreshToken(
    @Id var token: UUID = UUID.randomUUID(),
    var subjectId: UUID,
    @Enumerated(EnumType.STRING) var subjectType: Role,
    var expiresAt: Instant,
)
