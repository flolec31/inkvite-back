package com.stencilia.stenciliaback.auth.artist.repository

import com.stencilia.stenciliaback.auth.artist.entity.PasswordResetToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface PasswordResetTokenRepository : JpaRepository<PasswordResetToken, String> {
    fun findByTattooArtistId(tattooArtistId: UUID): PasswordResetToken?
}
