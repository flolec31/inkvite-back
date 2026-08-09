package com.inkvite.inkviteback.support.service

import com.inkvite.inkviteback.storage.dto.ImageUploadResponseDto
import com.inkvite.inkviteback.support.dto.SupportMessageRequestDto
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

interface SupportMessageService {
    fun uploadScreenshot(artistId: UUID, photo: MultipartFile): ImageUploadResponseDto
    fun submit(artistId: UUID, request: SupportMessageRequestDto)
}
