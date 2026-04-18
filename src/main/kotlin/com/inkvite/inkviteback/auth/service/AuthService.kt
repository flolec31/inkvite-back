package com.inkvite.inkviteback.auth.service

import com.inkvite.inkviteback.auth.dto.LoginResponseDto

interface AuthService {
    fun register(email: String, password: String)
    fun verify(token: String)
    fun resendVerification(email: String)
    fun login(email: String, password: String): LoginResponseDto
    fun refresh(refreshToken: String): LoginResponseDto
    fun logout(refreshToken: String)
}
