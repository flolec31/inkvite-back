package com.stencilia.stenciliaback.auth.artist.dto

import jakarta.validation.constraints.NotBlank

data class LogoutRequestDto(
    @field:NotBlank val refreshToken: String,
)
