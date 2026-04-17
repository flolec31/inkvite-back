package com.inkvite.inkviteback.auth.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class LoginRequestDto(
    @field:NotBlank @field:Email val email: String,
    @field:NotBlank val password: String,
)
