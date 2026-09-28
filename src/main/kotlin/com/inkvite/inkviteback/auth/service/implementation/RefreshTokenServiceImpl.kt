package com.inkvite.inkviteback.auth.service.implementation

import com.inkvite.inkviteback.auth.Role
import com.inkvite.inkviteback.auth.dto.LoginResponseDto
import com.inkvite.inkviteback.auth.entity.RefreshToken
import com.inkvite.inkviteback.auth.exception.InvalidRefreshTokenException
import com.inkvite.inkviteback.auth.repository.RefreshTokenRepository
import com.inkvite.inkviteback.auth.service.JwtService
import com.inkvite.inkviteback.auth.service.RefreshTokenService
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
