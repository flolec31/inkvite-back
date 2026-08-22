package com.inkvite.inkviteback.storage

import com.inkvite.inkviteback.storage.exception.ImageTooLargeException
import com.inkvite.inkviteback.storage.exception.InvalidImageContentTypeException
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

/**
 * Single source of truth for image upload validation, shared by every image entry point
 * (appointment references, support screenshots, message images, artist profile photos).
 */
@Component
class ImageValidator {

    fun validate(photo: MultipartFile) {
        if (photo.contentType !in ALLOWED_CONTENT_TYPES) throw InvalidImageContentTypeException()
        if (photo.size > MAX_SIZE_BYTES) throw ImageTooLargeException()
    }

    companion object {
        val ALLOWED_CONTENT_TYPES = setOf("image/jpeg", "image/png", "image/webp")
        const val MAX_SIZE_BYTES = 5L * 1024 * 1024
    }
}