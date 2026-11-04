package com.stencilia.stenciliaback.support.dto

import com.stencilia.stenciliaback.artist.entity.TattooArtist
import com.stencilia.stenciliaback.support.entity.SupportMessage
import com.stencilia.stenciliaback.support.entity.SupportMessageType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant

data class SupportMessageRequestDto(
    val type: SupportMessageType,
    @field:NotBlank @field:Size(max = 1500) val message: String,
    @field:Size(max = 1024) val screenshot: String? = null,
) {
    fun toEntity(artist: TattooArtist): SupportMessage =
        SupportMessage(
            artist = artist,
            type = type,
            message = message,
            screenshotKey = screenshot,
            createdAt = Instant.now(),
        )
}
