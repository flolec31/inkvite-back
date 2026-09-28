package com.inkvite.inkviteback.auth.artist.service.implementation

import com.inkvite.inkviteback.artist.exception.SlugAlreadyTakenException
import com.inkvite.inkviteback.artist.exception.TattooArtistAlreadyExistsException
import com.inkvite.inkviteback.artist.service.TattooArtistService
import com.inkvite.inkviteback.auth.artist.dto.ChangePasswordRequestDto
import com.inkvite.inkviteback.auth.dto.LoginResponseDto
import com.inkvite.inkviteback.auth.artist.dto.RegisterRequestDto
import com.inkvite.inkviteback.auth.artist.dto.ResetPasswordRequestDto
import com.inkvite.inkviteback.auth.artist.entity.PasswordResetToken
import com.inkvite.inkviteback.auth.artist.entity.VerificationToken
import com.inkvite.inkviteback.auth.artist.event.ArtistVerificationEmailRequested
import com.inkvite.inkviteback.auth.artist.event.PasswordChangedEmailRequested
import com.inkvite.inkviteback.auth.artist.event.PasswordResetEmailRequested
import com.inkvite.inkviteback.auth.exception.*
import com.inkvite.inkviteback.auth.artist.exception.*
import com.inkvite.inkviteback.auth.artist.repository.PasswordResetTokenRepository
import com.inkvite.inkviteback.auth.artist.repository.VerificationTokenRepository
import com.inkvite.inkviteback.auth.Role
import com.inkvite.inkviteback.auth.artist.service.AuthService
import com.inkvite.inkviteback.auth.service.RefreshTokenService
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
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val tattooArtistService: TattooArtistService,
    private val passwordEncoder: PasswordEncoder,
    private val refreshTokenService: RefreshTokenService,
) : AuthService {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun register(request: RegisterRequestDto) {
        tattooArtistService.findUnactivatedByEmail(request.email)?.let { unverified ->
            tokenRepository.findByTattooArtistId(unverified.id)?.let { tokenRepository.delete(it) }
            tattooArtistService.delete(unverified.id)
        }
        if (tattooArtistService.isSlugTaken(request.slug)) throw SlugAlreadyTakenException()
        tattooArtistService.findUnactivatedBySlug(request.slug)?.let { unverified ->
            tokenRepository.findByTattooArtistId(unverified.id)?.let { tokenRepository.delete(it) }
            tattooArtistService.delete(unverified.id)
        }
        val encodedPassword = passwordEncoder.encode(request.password)!!
        val artistId = try {
            tattooArtistService.register(request.toModel(encodedPassword))
        } catch (_: TattooArtistAlreadyExistsException) {
            throw EmailAlreadyRegisteredException()
        }
        val verificationToken = VerificationToken(tattooArtistId = artistId)
        val token = tokenRepository.save(verificationToken).token
        eventPublisher.publishEvent(ArtistVerificationEmailRequested(request.email, request.artistName, token))
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
        return refreshTokenService.issue(verificationToken.tattooArtistId, Role.ARTIST)
    }

    override fun login(email: String, password: String): LoginResponseDto {
        val artist = tattooArtistService.findByEmail(email) ?: throw InvalidCredentialsException()
        if (!passwordEncoder.matches(password, artist.password)) throw InvalidCredentialsException()
        if (artist.activatedAt == null) throw AccountNotActivatedException()
        return refreshTokenService.issue(artist.id, Role.ARTIST)
    }

    @Transactional(noRollbackFor = [InvalidRefreshTokenException::class])
    override fun refresh(refreshToken: String): LoginResponseDto =
        refreshTokenService.rotate(refreshToken, Role.ARTIST)

    override fun logout(refreshToken: String) = refreshTokenService.revoke(refreshToken)

    override fun forgotPassword(email: String) {
        val artist = tattooArtistService.findByEmail(email)?.takeIf { it.activatedAt != null } ?: return
        passwordResetTokenRepository.findByTattooArtistId(artist.id)?.let { passwordResetTokenRepository.delete(it) }
        val resetToken = PasswordResetToken(tattooArtistId = artist.id)
        val token = passwordResetTokenRepository.save(resetToken).token
        eventPublisher.publishEvent(PasswordResetEmailRequested(artist.email, artist.artistName, token))
    }

    override fun changePassword(artistId: UUID, request: ChangePasswordRequestDto): LoginResponseDto {
        val artist = tattooArtistService.findById(artistId)
        if (!passwordEncoder.matches(request.currentPassword, artist.password)) throw InvalidCredentialsException()
        val encodedNewPassword = passwordEncoder.encode(request.newPassword)!!
        tattooArtistService.updatePassword(artistId, encodedNewPassword)
        refreshTokenService.revokeAll(artistId, Role.ARTIST)
        eventPublisher.publishEvent(PasswordChangedEmailRequested(artist.email, artist.artistName))
        return refreshTokenService.issue(artistId, Role.ARTIST)
    }

    // noRollbackFor: TokenExpiredException must not roll back the transaction so the deletion below persists.
    @Transactional(noRollbackFor = [TokenExpiredException::class])
    override fun resetPassword(request: ResetPasswordRequestDto): LoginResponseDto {
        val resetToken = passwordResetTokenRepository.findById(request.token).orElse(null)
            ?: throw TokenNotFoundException()
        if (resetToken.expiresAt.isBefore(Instant.now())) {
            passwordResetTokenRepository.delete(resetToken)
            throw TokenExpiredException()
        }
        passwordResetTokenRepository.delete(resetToken)
        val encodedNewPassword = passwordEncoder.encode(request.newPassword)!!
        val artist = tattooArtistService.findById(resetToken.tattooArtistId)
        tattooArtistService.updatePassword(resetToken.tattooArtistId, encodedNewPassword)
        refreshTokenService.revokeAll(resetToken.tattooArtistId, Role.ARTIST)
        eventPublisher.publishEvent(PasswordChangedEmailRequested(artist.email, artist.artistName))
        return refreshTokenService.issue(resetToken.tattooArtistId, Role.ARTIST)
    }
}
