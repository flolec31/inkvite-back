package com.stencilia.stenciliaback.auth.repository

import com.stencilia.stenciliaback.auth.Role
import com.stencilia.stenciliaback.auth.entity.RefreshToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshToken, UUID> {
    fun deleteAllBySubjectIdAndSubjectType(subjectId: UUID, subjectType: Role)
}
