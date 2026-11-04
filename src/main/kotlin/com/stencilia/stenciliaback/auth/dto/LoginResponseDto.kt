package com.stencilia.stenciliaback.auth.dto

data class LoginResponseDto(
    val accessToken: String,
    val refreshToken: String,
)
