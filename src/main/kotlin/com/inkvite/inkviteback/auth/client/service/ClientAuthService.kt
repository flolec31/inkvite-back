package com.inkvite.inkviteback.auth.client.service

import com.inkvite.inkviteback.auth.dto.LoginResponseDto
import java.util.UUID

interface ClientAuthService {
    fun requestCode(appointmentId: UUID)
    fun verifyCode(appointmentId: UUID, code: String): LoginResponseDto
    fun refresh(refreshToken: String): LoginResponseDto
}
