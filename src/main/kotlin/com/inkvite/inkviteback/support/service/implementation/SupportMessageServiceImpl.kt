package com.inkvite.inkviteback.support.service.implementation

import com.inkvite.inkviteback.artist.service.TattooArtistService
import com.inkvite.inkviteback.storage.dto.ImageUploadResponseDto
import com.inkvite.inkviteback.storage.service.ImageUploadService
import com.inkvite.inkviteback.support.dto.SupportMessageRequestDto
import com.inkvite.inkviteback.support.repository.SupportMessageRepository
import com.inkvite.inkviteback.support.service.SupportMessageService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@Service
@Transactional(readOnly = true)
class SupportMessageServiceImpl(
    private val tattooArtistService: TattooArtistService,
    private val imageUploadService: ImageUploadService,
    private val supportMessageRepository: SupportMessageRepository,
) : SupportMessageService {

    override fun uploadScreenshot(artistId: UUID, photo: MultipartFile): ImageUploadResponseDto =
        imageUploadService.uploadSupportScreenshot(artistId, photo)

    @Transactional
    override fun submit(artistId: UUID, request: SupportMessageRequestDto) {
        val artist = tattooArtistService.findById(artistId)
        supportMessageRepository.save(request.toEntity(artist))
    }
}
