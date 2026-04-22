package com.inkvite.inkviteback.appointment.dto

import com.inkvite.inkviteback.appointment.model.CommentedReferenceModel
import jakarta.validation.constraints.NotBlank

data class ReferenceRequestDto(
    @field:NotBlank val key: String,
    val comment: String? = null,
) {
    fun toModel(): CommentedReferenceModel =
        CommentedReferenceModel(key = key, comment = comment)
}