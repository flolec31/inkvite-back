package com.stencilia.stenciliaback.appointment.dto

import com.stencilia.stenciliaback.appointment.entity.Reference
import java.util.*

data class ReferenceDetailsResponseDto(
    val id: UUID,
    val url: String,
    val comment: String?,
) {
    constructor(reference: Reference, url: String) : this(
        id = reference.id,
        url = url,
        comment = reference.comment,
    )
}