package com.stencilia.stenciliaback.support.service

import com.stencilia.stenciliaback.storage.dto.ImageUploadResponseDto
import com.stencilia.stenciliaback.support.dto.SupportMessageRequestDto
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

interface SupportMessageService {
    fun uploadScreenshot(artistId: UUID, photo: MultipartFile): ImageUploadResponseDto
    fun submit(artistId: UUID, request: SupportMessageRequestDto)
}
