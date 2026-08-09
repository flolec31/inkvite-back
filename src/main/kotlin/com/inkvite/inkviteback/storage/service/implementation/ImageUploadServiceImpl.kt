package com.inkvite.inkviteback.storage.service.implementation

import com.inkvite.inkviteback.storage.dto.ImageUploadResponseDto
import com.inkvite.inkviteback.storage.exception.ImageTooLargeException
import com.inkvite.inkviteback.storage.exception.ImageUploadFailedException
import com.inkvite.inkviteback.storage.exception.InvalidImageContentTypeException
import com.inkvite.inkviteback.storage.service.ImageUploadService
import com.inkvite.inkviteback.storage.service.StorageService
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@Service
class ImageUploadServiceImpl(
    private val storageService: StorageService
) : ImageUploadService {

    override fun uploadReference(artistId: UUID, photo: MultipartFile): ImageUploadResponseDto =
        upload("references", artistId, photo)

    override fun uploadSupportScreenshot(artistId: UUID, photo: MultipartFile): ImageUploadResponseDto =
        upload("contact-screenshots", artistId, photo)

    private fun upload(prefix: String, artistId: UUID, photo: MultipartFile): ImageUploadResponseDto {
        val allowedTypes = setOf("image/jpeg", "image/png", "image/webp")
        if (photo.contentType !in allowedTypes) throw InvalidImageContentTypeException()
        if (photo.size > 5 * 1024 * 1024) throw ImageTooLargeException()

        val key = "$prefix/$artistId/${UUID.randomUUID()}"
        val url = try {
            storageService.upload(key, photo.bytes, photo.contentType!!)
        } catch (e: Exception) {
            throw ImageUploadFailedException(e)
        }
        return ImageUploadResponseDto(key = key, url = url)
    }
}
