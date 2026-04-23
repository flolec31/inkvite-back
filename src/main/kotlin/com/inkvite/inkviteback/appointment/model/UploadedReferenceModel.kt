package com.inkvite.inkviteback.appointment.model

import com.inkvite.inkviteback.appointment.dto.ReferenceResponseDto

data class UploadedReferenceModel(
    val key: String,
    val url: String
) {
    fun toDto(): ReferenceResponseDto =
        ReferenceResponseDto(key = key, url = url)
}
