package com.stencilia.stenciliaback.auth.artist.repository

import com.stencilia.stenciliaback.auth.artist.entity.VerificationToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface VerificationTokenRepository : JpaRepository<VerificationToken, String> {
    fun findByTattooArtistId(tattooArtistId: UUID): VerificationToken?
}