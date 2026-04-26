package com.inkvite.inkviteback.auth.repository

import com.inkvite.inkviteback.auth.entity.PasswordResetToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface PasswordResetTokenRepository : JpaRepository<PasswordResetToken, String> {
    fun findByTattooArtistId(tattooArtistId: UUID): PasswordResetToken?
}
