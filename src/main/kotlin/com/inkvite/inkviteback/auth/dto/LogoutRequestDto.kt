package com.inkvite.inkviteback.auth.dto

import jakarta.validation.constraints.NotBlank

data class LogoutRequestDto(
    @field:NotBlank val refreshToken: String,
)
