package com.inkvite.inkviteback.auth.client.dto

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import java.util.UUID

data class VerifyCodeRequestDto(
    @field:NotNull var appointmentId: UUID,
    @field:Pattern(regexp = "\\d{6}") val code: String
)
