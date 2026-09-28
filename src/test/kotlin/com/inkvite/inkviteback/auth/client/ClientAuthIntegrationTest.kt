package com.inkvite.inkviteback.auth.client

import com.inkvite.inkviteback.appointment.AbstractAppointmentIntegrationTest
import com.inkvite.inkviteback.auth.Role
import com.inkvite.inkviteback.auth.client.dto.ClientRefreshRequestDto
import com.inkvite.inkviteback.auth.client.dto.RequestCodeRequestDto
import com.inkvite.inkviteback.auth.client.dto.VerifyCodeRequestDto
import com.inkvite.inkviteback.auth.client.entity.ClientAccessCode
import com.inkvite.inkviteback.auth.client.repository.ClientAccessCodeRepository
import com.inkvite.inkviteback.auth.repository.RefreshTokenRepository
import com.inkvite.inkviteback.email.service.EmailService
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.ObjectMapper
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

class ClientAuthIntegrationTest : AbstractAppointmentIntegrationTest() {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var objectMapper: ObjectMapper
    @Autowired lateinit var clientAccessCodeRepository: ClientAccessCodeRepository
    @Autowired lateinit var refreshTokenRepository: RefreshTokenRepository
    @Autowired lateinit var jwtDecoder: JwtDecoder

    @MockitoBean lateinit var emailService: EmailService

    // Runs before the parent's @AfterEach (which deletes clients), so access codes are
    // removed before their FK target — avoids a foreign-key violation between tests.
    @org.junit.jupiter.api.AfterEach
    fun cleanupClientAuth() {
        clientAccessCodeRepository.deleteAll()
    }

    @org.junit.jupiter.api.AfterEach
    fun cleanupClientAuthRefreshTokens() {
        refreshTokenRepository.deleteAll()
    }

    private fun requestCode(appointmentId: UUID) = mockMvc.perform(
        post("/auth/client/request-code")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(RequestCodeRequestDto(appointmentId)))
    )

    private fun verifyCode(appointmentId: UUID, code: String) = mockMvc.perform(
        post("/auth/client/verify-code")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(VerifyCodeRequestDto(appointmentId, code)))
    )

    private fun seedCode(
        clientId: UUID,
        code: String = "123456",
        attempts: Int = 0,
        expiresAt: Instant = Instant.now().plus(10, ChronoUnit.MINUTES),
        lastSentAt: Instant = Instant.now(),
    ) = clientAccessCodeRepository.save(ClientAccessCode(clientId, code, attempts, expiresAt, lastSentAt))

    @Test
    fun `request-code stores a code and emails it for a real appointment`() {
        val artist = createActivatedArtist()
        val client = createClient()
        val appointment = createAppointment(artist, client)

        requestCode(appointment.id).andExpect(status().isNoContent)

        val stored = clientAccessCodeRepository.findById(client.id).orElseThrow()
        assertThat(stored.code).matches("\\d{6}")
        assertThat(stored.attempts).isZero()
        verify(emailService).sendClientAccessCodeEmail(client.email, client.firstName, stored.code)
    }

    @Test
    fun `request-code returns 204 and does nothing for an unknown appointment`() {
        requestCode(UUID.randomUUID()).andExpect(status().isNoContent)
        assertThat(clientAccessCodeRepository.count()).isZero()
        verifyNoInteractions(emailService)
    }

    @Test
    fun `request-code within the cooldown resends the same code without a second email`() {
        val artist = createActivatedArtist()
        val client = createClient()
        val appointment = createAppointment(artist, client)

        requestCode(appointment.id).andExpect(status().isNoContent)
        val first = clientAccessCodeRepository.findById(client.id).orElseThrow().code
        requestCode(appointment.id).andExpect(status().isNoContent)

        assertThat(clientAccessCodeRepository.count()).isEqualTo(1)
        val second = clientAccessCodeRepository.findById(client.id).orElseThrow().code
        assertThat(second).isEqualTo(first)
        // the second request is inside the cooldown, so no second email is sent
        verify(emailService, times(1)).sendClientAccessCodeEmail(client.email, client.firstName, first)
    }

    @Test
    fun `request-code past the cooldown resends the still-valid code without resetting attempts`() {
        val artist = createActivatedArtist()
        val client = createClient()
        val appointment = createAppointment(artist, client)
        seedCode(client.id, "123456", attempts = 3, lastSentAt = Instant.now().minusSeconds(120))

        requestCode(appointment.id).andExpect(status().isNoContent)

        val row = clientAccessCodeRepository.findById(client.id).orElseThrow()
        assertThat(row.code).isEqualTo("123456")
        assertThat(row.attempts).isEqualTo(3)
        verify(emailService).sendClientAccessCodeEmail(client.email, client.firstName, "123456")
    }

    @Test
    fun `request-code with an expired code mints a fresh code and resets attempts`() {
        val artist = createActivatedArtist()
        val client = createClient()
        val appointment = createAppointment(artist, client)
        seedCode(client.id, "123456", attempts = 4, expiresAt = Instant.now().minusSeconds(1))

        requestCode(appointment.id).andExpect(status().isNoContent)

        val row = clientAccessCodeRepository.findById(client.id).orElseThrow()
        assertThat(row.attempts).isZero()
        assertThat(row.expiresAt).isAfter(Instant.now())
        // the freshly minted code is the one that was emailed
        verify(emailService).sendClientAccessCodeEmail(client.email, client.firstName, row.code)
    }

    @Test
    fun `request-code within the cooldown does not extend a code toward brute-force`() {
        val artist = createActivatedArtist()
        val client = createClient()
        val appointment = createAppointment(artist, client)
        // a valid code with 3 failed attempts, just sent
        seedCode(client.id, "123456", attempts = 3, lastSentAt = Instant.now())

        requestCode(appointment.id).andExpect(status().isNoContent)

        // attempts are NOT reset and no email is resent inside the cooldown
        val row = clientAccessCodeRepository.findById(client.id).orElseThrow()
        assertThat(row.code).isEqualTo("123456")
        assertThat(row.attempts).isEqualTo(3)
        verify(emailService, never()).sendClientAccessCodeEmail(anyString(), anyString(), anyString())
    }

    @Test
    fun `concurrent writes to an access code are rejected by optimistic locking`() {
        val artist = createActivatedArtist()
        val client = createClient()
        createAppointment(artist, client)
        seedCode(client.id, "123456")

        // two independent transactions load the same row, then both try to persist a change
        val a = clientAccessCodeRepository.findById(client.id).orElseThrow()
        val b = clientAccessCodeRepository.findById(client.id).orElseThrow()
        a.attempts = 1
        clientAccessCodeRepository.saveAndFlush(a)
        b.attempts = 2

        assertThatThrownBy { clientAccessCodeRepository.saveAndFlush(b) }
            .isInstanceOf(ObjectOptimisticLockingFailureException::class.java)
    }

    @Test
    fun `verify-code returns a client token pair for a valid code`() {
        val artist = createActivatedArtist(); val client = createClient()
        val appointment = createAppointment(artist, client)
        seedCode(client.id, "123456")

        val result = verifyCode(appointment.id, "123456")
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.accessToken").exists())
            .andExpect(jsonPath("$.refreshToken").exists())
            .andReturn()

        val node = objectMapper.readTree(result.response.contentAsString)
        val jwt = jwtDecoder.decode(node.get("accessToken").asString())
        assertThat(jwt.subject).isEqualTo(client.id.toString())
        assertThat(jwt.getClaimAsString("type")).isEqualTo("client")
        assertThat(clientAccessCodeRepository.findById(client.id)).isEmpty
        val refresh = refreshTokenRepository.findAll().single()
        assertThat(refresh.subjectId).isEqualTo(client.id)
        assertThat(refresh.subjectType).isEqualTo(Role.CLIENT)
    }

    @Test
    fun `verify-code with a wrong code increments attempts and deletes the row on the fifth`() {
        val artist = createActivatedArtist(); val client = createClient()
        val appointment = createAppointment(artist, client)
        seedCode(client.id, "123456", attempts = 4)

        verifyCode(appointment.id, "000000").andExpect(status().isUnauthorized)

        assertThat(clientAccessCodeRepository.findById(client.id)).isEmpty
    }

    @Test
    fun `verify-code with an expired code is rejected and the row deleted`() {
        val artist = createActivatedArtist(); val client = createClient()
        val appointment = createAppointment(artist, client)
        seedCode(client.id, "123456", expiresAt = Instant.now().minusSeconds(1))

        verifyCode(appointment.id, "123456").andExpect(status().isUnauthorized)

        assertThat(clientAccessCodeRepository.findById(client.id)).isEmpty
    }

    @Test
    fun `a client access token is rejected on an artist route`() {
        val artist = createActivatedArtist(); val client = createClient()
        val appointment = createAppointment(artist, client)
        seedCode(client.id, "123456")
        val token = objectMapper.readTree(
            verifyCode(appointment.id, "123456").andReturn().response.contentAsString
        ).get("accessToken").asString()

        mockMvc.perform(get("/artists/me").header("Authorization", "Bearer $token"))
            .andExpect(status().isForbidden)
    }

    private fun issuePair(appointmentId: UUID): Pair<String, String> {
        val body = objectMapper.readTree(verifyCode(appointmentId, "123456").andReturn().response.contentAsString)
        return body.get("accessToken").asString() to body.get("refreshToken").asString()
    }

    @Test
    fun `refresh rotates the client token pair and invalidates the old refresh token`() {
        val artist = createActivatedArtist(); val client = createClient()
        val appointment = createAppointment(artist, client)
        seedCode(client.id, "123456")
        val (_, oldRefresh) = issuePair(appointment.id)

        mockMvc.perform(
            post("/auth/client/refresh").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ClientRefreshRequestDto(oldRefresh)))
        ).andExpect(status().isOk)

        // old refresh token no longer works
        mockMvc.perform(
            post("/auth/client/refresh").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ClientRefreshRequestDto(oldRefresh)))
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `refresh rejects an unknown token`() {
        mockMvc.perform(
            post("/auth/client/refresh").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ClientRefreshRequestDto(UUID.randomUUID().toString())))
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `refresh rejects an artist-typed refresh token`() {
        // an artist refresh row must not be usable on the client refresh route
        val artist = createActivatedArtist(); val client = createClient()
        val appointment = createAppointment(artist, client)
        seedCode(client.id, "123456")
        val (_, clientRefresh) = issuePair(appointment.id)
        // flip the stored row to ARTIST to simulate a cross-audience token
        val row = refreshTokenRepository.findAll().single()
        row.subjectType = Role.ARTIST
        refreshTokenRepository.save(row)

        mockMvc.perform(
            post("/auth/client/refresh").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ClientRefreshRequestDto(clientRefresh)))
        ).andExpect(status().isUnauthorized)

        // the token itself is still valid (just wrong audience) and must survive the rejection
        assertThat(refreshTokenRepository.findById(row.token)).isPresent
    }
}
