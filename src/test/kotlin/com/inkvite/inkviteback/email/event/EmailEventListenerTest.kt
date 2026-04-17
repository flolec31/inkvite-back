package com.inkvite.inkviteback.email.event

import com.inkvite.inkviteback.auth.event.VerificationEmailRequested
import com.inkvite.inkviteback.email.service.EmailService
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
        val event = VerificationEmailRequested(to = "user@example.com", token = "abc123")

        listener.on(event)

        verify(emailService).sendVerificationEmail("user@example.com", "abc123")
    }
}