package com.inkvite.inkviteback.auth.dto

import jakarta.validation.constraints.NotBlank

data class RefreshRequestDto(
    @field:NotBlank val refreshToken: String,
)
