package com.stencilia.stenciliaback.auth.client.dto

import jakarta.validation.constraints.NotNull
import java.util.UUID

data class RequestCodeRequestDto(
    @field:NotNull var appointmentId: UUID
)
