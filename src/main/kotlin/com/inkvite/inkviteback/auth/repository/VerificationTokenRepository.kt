package com.inkvite.inkviteback.auth.repository

import com.inkvite.inkviteback.auth.entity.VerificationToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface VerificationTokenRepository : JpaRepository<VerificationToken, String> {
    fun findByToken(token: String): VerificationToken?
}