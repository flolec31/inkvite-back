package com.stencilia.stenciliaback.auth.service.implementation

import com.stencilia.stenciliaback.auth.Role
import com.stencilia.stenciliaback.auth.dto.LoginResponseDto
import com.stencilia.stenciliaback.auth.entity.RefreshToken
import com.stencilia.stenciliaback.auth.exception.InvalidRefreshTokenException
import com.stencilia.stenciliaback.auth.repository.RefreshTokenRepository
import com.stencilia.stenciliaback.auth.service.JwtService
import com.stencilia.stenciliaback.auth.service.RefreshTokenService
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class RefreshTokenServiceImpl(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtService: JwtService,
) : RefreshTokenService {

    override fun issue(subjectId: UUID, role: Role): LoginResponseDto {
        val refreshToken = RefreshToken(
            subjectId = subjectId,
            subjectType = role,
            expiresAt = Instant.now().plus(role.refreshTokenDuration),
        )
        refreshTokenRepository.save(refreshToken)
        return LoginResponseDto(
            accessToken = jwtService.generateAccessToken(subjectId, role),
            refreshToken = refreshToken.token.toString(),
        )
    }

    override fun rotate(rawToken: String, role: Role): LoginResponseDto {
        val tokenId = runCatching { UUID.fromString(rawToken) }.getOrElse { throw InvalidRefreshTokenException() }
        val token = refreshTokenRepository.findById(tokenId).orElse(null) ?: throw InvalidRefreshTokenException()
        if (token.subjectType != role) {
            throw InvalidRefreshTokenException()
        }
        if (token.expiresAt.isBefore(Instant.now())) {
            refreshTokenRepository.delete(token)
            throw InvalidRefreshTokenException()
        }
        refreshTokenRepository.delete(token)
        return issue(token.subjectId, role)
    }

    override fun revoke(rawToken: String) {
        val tokenId = runCatching { UUID.fromString(rawToken) }.getOrElse { return }
        refreshTokenRepository.findById(tokenId).ifPresent { refreshTokenRepository.delete(it) }
    }

    override fun revokeAll(subjectId: UUID, role: Role) {
        refreshTokenRepository.deleteAllBySubjectIdAndSubjectType(subjectId, role)
    }
}
