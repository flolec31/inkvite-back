package com.inkvite.inkviteback.auth.client.service.implementation

import com.inkvite.inkviteback.appointment.repository.AppointmentRepository
import com.inkvite.inkviteback.auth.Role
import com.inkvite.inkviteback.auth.client.entity.ClientAccessCode
import com.inkvite.inkviteback.auth.client.event.ClientAccessCodeRequested
import com.inkvite.inkviteback.auth.client.exception.InvalidCodeException
import com.inkvite.inkviteback.auth.client.repository.ClientAccessCodeRepository
import com.inkvite.inkviteback.auth.client.service.ClientAuthService
import com.inkvite.inkviteback.auth.dto.LoginResponseDto
import com.inkvite.inkviteback.auth.exception.InvalidRefreshTokenException
import com.inkvite.inkviteback.auth.service.RefreshTokenService
import com.inkvite.inkviteback.client.entity.TattooClient
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.SecureRandom
import java.time.Duration
import java.time.Instant
import java.util.UUID

@Service
@Transactional(readOnly = true)
class ClientAuthServiceImpl(
    private val appointmentRepository: AppointmentRepository,
    private val clientAccessCodeRepository: ClientAccessCodeRepository,
    private val eventPublisher: ApplicationEventPublisher,
    private val refreshTokenService: RefreshTokenService,
) : ClientAuthService {

    private val random = SecureRandom()

    companion object {
        private const val MAX_ATTEMPTS = 5
        private val CODE_VALIDITY = Duration.ofMinutes(10)
        private val RESEND_COOLDOWN = Duration.ofSeconds(60)
    }

    @Transactional
    override fun requestCode(appointmentId: UUID) {
        val appointment = appointmentRepository.findById(appointmentId).orElse(null) ?: return
        val client = appointment.client
        val now = Instant.now()
        val existing = clientAccessCodeRepository.findById(client.id).orElse(null)

        if (existing != null && existing.expiresAt.isAfter(now)) {
            // A valid code already exists: never reset attempts (so brute-force stays capped
            // over the code's whole lifetime), and only re-send the email past the cooldown.
            if (existing.lastSentAt.isBefore(now.minus(RESEND_COOLDOWN))) {
                existing.lastSentAt = now
                clientAccessCodeRepository.save(existing)
                publishCode(client, existing.code)
            }
            return
        }

        // No code, or the previous one expired: mint a fresh code and reset attempts.
        val code = generateCode()
        val accessCode = existing?.apply {
            this.code = code
            this.attempts = 0
            this.expiresAt = now.plus(CODE_VALIDITY)
            this.lastSentAt = now
        } ?: ClientAccessCode(clientId = client.id, code = code, lastSentAt = now)
        clientAccessCodeRepository.save(accessCode)
        publishCode(client, code)
    }

    private fun publishCode(client: TattooClient, code: String) =
        eventPublisher.publishEvent(ClientAccessCodeRequested(client.email, client.firstName, code))

    @Transactional(noRollbackFor = [InvalidCodeException::class])
    override fun verifyCode(appointmentId: UUID, code: String): LoginResponseDto {
        val appointment = appointmentRepository.findById(appointmentId).orElse(null) ?: throw InvalidCodeException()
        val client = appointment.client
        val accessCode = clientAccessCodeRepository.findById(client.id).orElse(null) ?: throw InvalidCodeException()
        if (accessCode.expiresAt.isBefore(Instant.now()) || accessCode.attempts >= MAX_ATTEMPTS) {
            clientAccessCodeRepository.delete(accessCode)
            throw InvalidCodeException()
        }
        if (accessCode.code != code) {
            accessCode.attempts++
            if (accessCode.attempts >= MAX_ATTEMPTS) clientAccessCodeRepository.delete(accessCode)
            else clientAccessCodeRepository.save(accessCode)
            throw InvalidCodeException()
        }
        clientAccessCodeRepository.delete(accessCode)
        return refreshTokenService.issue(client.id, Role.CLIENT)
    }

    @Transactional(noRollbackFor = [InvalidRefreshTokenException::class])
    override fun refresh(refreshToken: String): LoginResponseDto =
        refreshTokenService.rotate(refreshToken, Role.CLIENT)

    private fun generateCode(): String = "%06d".format(random.nextInt(1_000_000))
}
