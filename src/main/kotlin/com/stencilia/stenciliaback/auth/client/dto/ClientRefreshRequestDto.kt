package com.stencilia.stenciliaback.auth.client.dto

import jakarta.validation.constraints.NotBlank

data class ClientRefreshRequestDto(
    @field:NotBlank val refreshToken: String,
)
