package com.inkvite.inkviteback.artist.dto

import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class UpdateProfileRequestDto(
    @field:Size(max = 100) val artistName: String?,
    @field:Pattern(regexp = "^[a-z0-9][a-z0-9-]{1,28}[a-z0-9]$") val slug: String?,
) {
    @Suppress("unused")
    @AssertTrue(message = "At least one field must be provided")
    fun isAtLeastOneFieldPresent(): Boolean = artistName != null || slug != null
}