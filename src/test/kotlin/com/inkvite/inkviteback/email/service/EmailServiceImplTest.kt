package com.inkvite.inkviteback.email.service

import com.inkvite.inkviteback.email.client.ResendEmailClient
import com.inkvite.inkviteback.email.service.implementation.EmailServiceImpl
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class EmailServiceImplTest {

    @Mock
    private lateinit var resendEmailClient: ResendEmailClient

    private lateinit var emailService: EmailServiceImpl

    @BeforeEach
    fun setUp() {
        emailService = EmailServiceImpl(resendEmailClient, "http://localhost:8080")
    }

    @Test
    fun `sendVerificationEmail builds verification link and delegates to client`() {
        emailService.sendVerificationEmail("user@example.com", "abc123")

        verify(resendEmailClient).sendEmail(
            "user@example.com",
            "verify-email",
            mapOf("link" to "http://localhost:8080/auth/verify?token=abc123")
        )
    }
}