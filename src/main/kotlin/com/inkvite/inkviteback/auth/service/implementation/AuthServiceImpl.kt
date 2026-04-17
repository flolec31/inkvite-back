package com.inkvite.inkviteback.auth.service.implementation

import com.inkvite.inkviteback.artist.exception.TattooArtistAlreadyExistsException
import com.inkvite.inkviteback.artist.service.TattooArtistService
import com.inkvite.inkviteback.auth.dto.LoginResponseDto
import com.inkvite.inkviteback.auth.entity.RefreshToken
import com.inkvite.inkviteback.auth.entity.VerificationToken
import com.inkvite.inkviteback.auth.event.VerificationEmailRequested
import com.inkvite.inkviteback.auth.exception.AccountNotActivatedException
import com.inkvite.inkviteback.auth.exception.EmailAlreadyRegisteredException
import com.inkvite.inkviteback.auth.exception.InvalidCredentialsException
import com.inkvite.inkviteback.auth.exception.InvalidRefreshTokenException
import com.inkvite.inkviteback.auth.exception.TokenExpiredException
import com.inkvite.inkviteback.auth.exception.TokenNotFoundException
import com.inkvite.inkviteback.auth.repository.RefreshTokenRepository
import com.inkvite.inkviteback.auth.repository.VerificationTokenRepository
import com.inkvite.inkviteback.auth.service.AuthService
import com.inkvite.inkviteback.auth.service.JwtService
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
@Transactional
class AuthServiceImpl(
    private val eventPublisher: ApplicationEventPublisher,
    private val tokenRepository: VerificationTokenRepository,
    private val tattooArtistService: TattooArtistService,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val refreshTokenRepository: RefreshTokenRepository,
) : AuthService {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun register(email: String, password: String) {
        val encodedPassword = passwordEncoder.encode(password)!!
        val artistId = try {
            tattooArtistService.register(email, encodedPassword)
        } catch (_: TattooArtistAlreadyExistsException) {
            throw EmailAlreadyRegisteredException()
        }
        val verificationToken = VerificationToken(tattooArtistId = artistId)
        tokenRepository.save(verificationToken)
        eventPublisher.publishEvent(VerificationEmailRequested(email, verificationToken.token))
    }

    override fun resendVerification(email: String) {
        val artistId = tattooArtistService.findUnactivatedByEmail(email) ?: return
        tokenRepository.findByTattooArtistId(artistId)?.let { tokenRepository.delete(it) }
        val verificationToken = VerificationToken(tattooArtistId = artistId)
        tokenRepository.save(verificationToken)
        logger.debug("Verification token updated for tattoo artist {}", email)
        eventPublisher.publishEvent(VerificationEmailRequested(email, verificationToken.token))
    }

    // noRollbackFor: TokenExpiredException must not roll back the transaction so the deletion below persists.
    @Transactional(noRollbackFor = [TokenExpiredException::class])
    override fun verify(token: String) {
        val verificationToken = tokenRepository.findById(token).orElse(null) ?: throw TokenNotFoundException()
        if (verificationToken.expiresAt.isBefore(Instant.now())) {
            tokenRepository.delete(verificationToken)
            throw TokenExpiredException()
        }
        tattooArtistService.activate(verificationToken.tattooArtistId)
        tokenRepository.delete(verificationToken)
    }

    override fun login(email: String, password: String): LoginResponseDto {
        val artist = tattooArtistService.findByEmail(email) ?: throw InvalidCredentialsException()
        if (!passwordEncoder.matches(password, artist.password)) throw InvalidCredentialsException()
        if (artist.activatedAt == null) throw AccountNotActivatedException()
        val refreshToken = RefreshToken(tattooArtistId = artist.id)
        refreshTokenRepository.save(refreshToken)
        return LoginResponseDto(
            accessToken = jwtService.generateAccessToken(artist.id),
            refreshToken = refreshToken.token.toString(),
        )
    }

    // noRollbackFor: InvalidRefreshTokenException must not roll back the transaction so the deletion below persists.
    @Transactional(noRollbackFor = [InvalidRefreshTokenException::class])
    override fun refresh(refreshToken: String): LoginResponseDto {
        val tokenUUID = runCatching { UUID.fromString(refreshToken) }.getOrElse { throw InvalidRefreshTokenException() }
        val token = refreshTokenRepository.findById(tokenUUID).orElse(null) ?: throw InvalidRefreshTokenException()
        if (token.expiresAt.isBefore(Instant.now())) {
            refreshTokenRepository.delete(token)
            throw InvalidRefreshTokenException()
        }
        refreshTokenRepository.delete(token)
        val newRefreshToken = RefreshToken(tattooArtistId = token.tattooArtistId)
        refreshTokenRepository.save(newRefreshToken)
        return LoginResponseDto(
            accessToken = jwtService.generateAccessToken(token.tattooArtistId),
            refreshToken = newRefreshToken.token.toString(),
        )
    }

    override fun logout(refreshToken: String) {
        val tokenUUID = runCatching { UUID.fromString(refreshToken) }.getOrElse { return }
        refreshTokenRepository.findById(tokenUUID).ifPresent { refreshTokenRepository.delete(it) }
    }
}
