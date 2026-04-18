package com.inkvite.inkviteback.auth.dto

data class LoginResponseDto(
    val accessToken: String,
    val refreshToken: String,
)
