package com.inkvite.inkviteback.auth.artist.repository

import com.inkvite.inkviteback.auth.artist.entity.PasswordResetToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface PasswordResetTokenRepository : JpaRepository<PasswordResetToken, String> {
    fun findByTattooArtistId(tattooArtistId: UUID): PasswordResetToken?
}
