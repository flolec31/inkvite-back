package com.inkvite.inkviteback.discussion.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class MessageRequestDto(
    @field:NotBlank @field:Size(max = 2000) val content: String,
)
