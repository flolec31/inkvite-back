package com.inkvite.inkviteback.auth.repository

import com.inkvite.inkviteback.auth.entity.VerificationToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface VerificationTokenRepository : JpaRepository<VerificationToken, String> {
    fun findByTattooArtistId(tattooArtistId: UUID): VerificationToken?
}