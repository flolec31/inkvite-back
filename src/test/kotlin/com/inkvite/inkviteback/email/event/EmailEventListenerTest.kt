package com.inkvite.inkviteback.email.event

import com.inkvite.inkviteback.appointment.event.AppointmentVerificationEmailRequested
import com.inkvite.inkviteback.auth.event.ArtistVerificationEmailRequested
import com.inkvite.inkviteback.email.service.EmailService
import java.util.UUID
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class EmailEventListenerTest {

    @Mock
    private lateinit var emailService: EmailService

    @InjectMocks
    private lateinit var listener: EmailEventListener

    @Test
    fun `on VerificationEmailRequested delegates to email service`() {
        val event = ArtistVerificationEmailRequested(to = "user@example.com", token = "abc123")

        listener.on(event)

        verify(emailService).sendArtistVerificationEmail("user@example.com", "abc123")
    }

    @Test
    fun `on AppointmentVerificationEmailRequested delegates to email service`() {
        val formId = UUID.randomUUID()
        val event = AppointmentVerificationEmailRequested(to = "client@example.com", form = formId)

        listener.on(event)

        verify(emailService).sendAppointmentVerificationEmail("client@example.com", formId)
    }
}