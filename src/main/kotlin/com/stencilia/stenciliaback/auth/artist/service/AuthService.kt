package com.stencilia.stenciliaback.auth.artist.service

import com.stencilia.stenciliaback.auth.artist.dto.ChangePasswordRequestDto
import com.stencilia.stenciliaback.auth.dto.LoginResponseDto
import com.stencilia.stenciliaback.auth.artist.dto.RegisterRequestDto
import com.stencilia.stenciliaback.auth.artist.dto.ResetPasswordRequestDto
import java.util.UUID

interface AuthService {
    fun register(request: RegisterRequestDto)
    fun resendVerification(email: String)
    fun verify(token: String): LoginResponseDto
    fun login(email: String, password: String): LoginResponseDto
    fun refresh(refreshToken: String): LoginResponseDto
    fun logout(refreshToken: String)
    fun forgotPassword(email: String)
    fun resetPassword(request: ResetPasswordRequestDto): LoginResponseDto
    fun changePassword(artistId: UUID, request: ChangePasswordRequestDto): LoginResponseDto
}
