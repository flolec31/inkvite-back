package com.stencilia.stenciliaback.auth.service

import com.stencilia.stenciliaback.auth.Role
import com.stencilia.stenciliaback.auth.dto.LoginResponseDto
import java.util.UUID

interface RefreshTokenService {

    /** Issues a fresh access + refresh token pair for the given subject. */
    fun issue(subjectId: UUID, role: Role): LoginResponseDto

    /**
     * Rotates a refresh token into a new pair. The caller controls rollback rules: the token
     * deletion must persist even when [com.stencilia.stenciliaback.auth.exception.InvalidRefreshTokenException]
     * is thrown, so the calling @Transactional method must annotate
     * noRollbackFor = [InvalidRefreshTokenException::class].
     */
    fun rotate(rawToken: String, role: Role): LoginResponseDto

    /** Revokes a single refresh token (no-op if it does not exist or is malformed). */
    fun revoke(rawToken: String)

    /** Revokes every refresh token for the given subject. */
    fun revokeAll(subjectId: UUID, role: Role)
}
