package com.inkvite.inkviteback.appointment.model

import com.inkvite.inkviteback.appointment.dto.ReferenceUploadResponseDto

data class UploadedReferenceModel(
    val key: String,
    val url: String
) {
    fun toDto(): ReferenceUploadResponseDto =
        ReferenceUploadResponseDto(key = key, url = url)
}
