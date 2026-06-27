package com.inkvite.inkviteback.appointment.dto

import com.inkvite.inkviteback.appointment.entity.Reference
import java.util.*

data class ReferenceDetailsResponseDto(
    val id: UUID,
    val url: String,
    val comment: String?,
    val crop: CropCoordinatesDto,
) {
    constructor(reference: Reference, url: String) : this(
        id = reference.id,
        url = url,
        comment = reference.comment,
        crop = CropCoordinatesDto(
            left = reference.cropLeft,
            top = reference.cropTop,
            width = reference.cropWidth,
            height = reference.cropHeight,
        )
    )
}