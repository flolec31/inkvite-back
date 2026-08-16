package com.inkvite.inkviteback.discussion.dto

import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.Size

data class MessageRequestDto(
    @field:Size(max = 2000) val content: String? = null,
    val imageKey: String? = null,
) {

    @Suppress("unused")
    @AssertTrue(message = "A message must have text or an image")
    fun isNotEmpty(): Boolean = !content.isNullOrBlank() || !imageKey.isNullOrBlank()
}
