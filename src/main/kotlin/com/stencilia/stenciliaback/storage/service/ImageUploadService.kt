package com.stencilia.stenciliaback.storage.service

import com.stencilia.stenciliaback.storage.dto.ImageUploadResponseDto
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

interface ImageUploadService {
    fun uploadReference(artistId: UUID, photo: MultipartFile): ImageUploadResponseDto
    fun uploadSupportScreenshot(artistId: UUID, photo: MultipartFile): ImageUploadResponseDto
    fun uploadMessageImage(artistId: UUID, photo: MultipartFile): ImageUploadResponseDto
}
