package com.inkvite.inkviteback.auth.service

import com.inkvite.inkviteback.auth.dto.ChangePasswordRequestDto
import com.inkvite.inkviteback.auth.dto.LoginResponseDto
import com.inkvite.inkviteback.auth.dto.ResetPasswordRequestDto
import java.util.UUID

interface AuthService {
    fun register(email: String, password: String, artistName: String, slug: String)
    fun resendVerification(email: String)
    fun verify(token: String): LoginResponseDto
    fun login(email: String, password: String): LoginResponseDto
    fun refresh(refreshToken: String): LoginResponseDto
    fun logout(refreshToken: String)
    fun forgotPassword(email: String)
    fun resetPassword(request: ResetPasswordRequestDto): LoginResponseDto
    fun changePassword(artistId: UUID, request: ChangePasswordRequestDto): LoginResponseDto
}
