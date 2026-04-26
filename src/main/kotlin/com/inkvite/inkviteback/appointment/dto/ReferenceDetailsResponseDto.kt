package com.inkvite.inkviteback.appointment.dto

import com.inkvite.inkviteback.appointment.entity.Reference
import java.util.*

data class ReferenceDetailsResponseDto(
    val id: UUID,
    val url: String,
    val comment: String?
) {
    constructor(reference: Reference, url: String) : this(
        id = reference.id,
        url = url,
        comment = reference.comment
    )
}