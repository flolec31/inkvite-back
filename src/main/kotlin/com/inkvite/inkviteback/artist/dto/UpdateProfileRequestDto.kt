package com.inkvite.inkviteback.artist.dto

import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class UpdateProfileRequestDto(
    @field:Size(max = 100) val artistName: String?,
    @field:Pattern(regexp = "^[a-z0-9][a-z0-9-]{1,28}[a-z0-9]$") val slug: String?,
    @field:Size(max = 100) val city: String?,
    @field:Pattern(regexp = "^[A-Z]{2}$") val countryCode: String?,
) {
    @Suppress("unused")
    @AssertTrue(message = "At least one field must be provided")
    fun isAtLeastOneFieldPresent(): Boolean =
        artistName != null || slug != null || city != null || countryCode != null
}