package com.inkvite.inkviteback.auth.service.implementation

import com.inkvite.inkviteback.artist.exception.SlugAlreadyTakenException
import com.inkvite.inkviteback.artist.exception.TattooArtistAlreadyExistsException
import com.inkvite.inkviteback.artist.service.TattooArtistService
import com.inkvite.inkviteback.auth.dto.LoginResponseDto
import com.inkvite.inkviteback.auth.entity.RefreshToken
import com.inkvite.inkviteback.auth.entity.VerificationToken
import com.inkvite.inkviteback.auth.event.ArtistVerificationEmailRequested
import com.inkvite.inkviteback.auth.exception.*
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
import java.util.*

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

    override fun register(email: String, password: String, artistName: String, slug: String) {
        if (tattooArtistService.existsBySlug(slug)) throw SlugAlreadyTakenException()
        val encodedPassword = passwordEncoder.encode(password)!!
        val artistId = try {
            tattooArtistService.register(email, encodedPassword, artistName, slug)
        } catch (_: TattooArtistAlreadyExistsException) {
            throw EmailAlreadyRegisteredException()
        }
        val verificationToken = VerificationToken(tattooArtistId = artistId)
        val token = tokenRepository.save(verificationToken).token
        eventPublisher.publishEvent(ArtistVerificationEmailRequested(email, artistName, token))
    }

    override fun resendVerification(email: String) {
        val artist = tattooArtistService.findUnactivatedByEmail(email) ?: return
        tokenRepository.findByTattooArtistId(artist.id)?.let { tokenRepository.delete(it) }
        val verificationToken = VerificationToken(tattooArtistId = artist.id)
        val token = tokenRepository.save(verificationToken).token
        logger.debug("Verification token updated for tattoo artist {}", email)
        eventPublisher.publishEvent(ArtistVerificationEmailRequested(email, artist.artistName, token))
    }

    // noRollbackFor: TokenExpiredException must not roll back the transaction so the deletion below persists.
    @Transactional(noRollbackFor = [TokenExpiredException::class])
    override fun verify(token: String): LoginResponseDto {
        val verificationToken = tokenRepository.findById(token).orElse(null) ?: throw TokenNotFoundException()
        if (verificationToken.expiresAt.isBefore(Instant.now())) {
            tokenRepository.delete(verificationToken)
            throw TokenExpiredException()
        }
        tattooArtistService.activate(verificationToken.tattooArtistId)
        tokenRepository.delete(verificationToken)
        return login(verificationToken.tattooArtistId)
    }

    override fun login(email: String, password: String): LoginResponseDto {
        val artist = tattooArtistService.findByEmail(email) ?: throw InvalidCredentialsException()
        if (!passwordEncoder.matches(password, artist.password)) throw InvalidCredentialsException()
        if (artist.activatedAt == null) throw AccountNotActivatedException()
        return login(artist.id)
    }

    private fun login(artistId: UUID): LoginResponseDto {
        val refreshToken = RefreshToken(tattooArtistId = artistId)
        refreshTokenRepository.save(refreshToken)
        return LoginResponseDto(
            accessToken = jwtService.generateAccessToken(artistId),
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
