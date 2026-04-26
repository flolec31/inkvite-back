package com.inkvite.inkviteback.auth.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ResetPasswordRequestDto(
    @field:NotBlank val token: String,
    @field:NotBlank @field:Size(min = 8) val newPassword: String,
)