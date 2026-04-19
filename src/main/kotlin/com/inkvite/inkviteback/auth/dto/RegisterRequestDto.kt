package com.inkvite.inkviteback.auth.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class RegisterRequestDto(
    @field:NotBlank @field:Email val email: String,
    @field:NotBlank @field:Size(min = 8) val password: String,
    @field:NotBlank @field:Size(max = 100) val artistName: String,
    @field:NotBlank @field:Pattern(regexp = "^[a-z0-9][a-z0-9-]{1,28}[a-z0-9]$") val slug: String,
)
