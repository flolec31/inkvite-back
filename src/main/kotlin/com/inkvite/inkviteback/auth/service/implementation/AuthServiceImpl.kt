package com.inkvite.inkviteback.auth.service.implementation

import com.inkvite.inkviteback.artist.exception.TattooArtistAlreadyExistsException
import com.inkvite.inkviteback.artist.service.TattooArtistService
import com.inkvite.inkviteback.auth.entity.VerificationToken
import com.inkvite.inkviteback.auth.event.VerificationEmailRequested
import com.inkvite.inkviteback.auth.exception.EmailAlreadyRegisteredException
import com.inkvite.inkviteback.auth.exception.TokenExpiredException
import com.inkvite.inkviteback.auth.exception.TokenNotFoundException
import com.inkvite.inkviteback.auth.repository.VerificationTokenRepository
import com.inkvite.inkviteback.auth.service.AuthService
import org.springframework.context.ApplicationEventPublisher
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@Service
@Transactional
class AuthServiceImpl(
    private val eventPublisher: ApplicationEventPublisher,
    private val tokenRepository: VerificationTokenRepository,
    private val tattooArtistService: TattooArtistService,
    private val passwordEncoder: PasswordEncoder,
) : AuthService {

    override fun register(email: String, password: String) {
        val encodedPassword = passwordEncoder.encode(password)!!
        val artistId = try {
            tattooArtistService.register(email, encodedPassword)
        } catch (_: TattooArtistAlreadyExistsException) {
            throw EmailAlreadyRegisteredException()
        }
        val verificationToken = VerificationToken(
            token = UUID.randomUUID().toString(),
            tattooArtistId = artistId,
            expiresAt = Instant.now().plus(24, ChronoUnit.HOURS),
        )
        tokenRepository.save(verificationToken)
        eventPublisher.publishEvent(VerificationEmailRequested(email, verificationToken.token))
    }

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
}
