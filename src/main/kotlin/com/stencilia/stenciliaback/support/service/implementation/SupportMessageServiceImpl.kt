package com.stencilia.stenciliaback.support.service.implementation

import com.stencilia.stenciliaback.artist.service.TattooArtistService
import com.stencilia.stenciliaback.storage.dto.ImageUploadResponseDto
import com.stencilia.stenciliaback.storage.service.ImageUploadService
import com.stencilia.stenciliaback.support.dto.SupportMessageRequestDto
import com.stencilia.stenciliaback.support.event.SupportMessageConfirmationEmailRequested
import com.stencilia.stenciliaback.support.event.SupportMessageReceivedEmailRequested
import com.stencilia.stenciliaback.support.repository.SupportMessageRepository
import com.stencilia.stenciliaback.support.service.SupportMessageService
import org.springframework.context.ApplicationEventPublisher
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
    private val eventPublisher: ApplicationEventPublisher,
) : SupportMessageService {

    override fun uploadScreenshot(artistId: UUID, photo: MultipartFile): ImageUploadResponseDto =
        imageUploadService.uploadSupportScreenshot(artistId, photo)

    @Transactional
    override fun submit(artistId: UUID, request: SupportMessageRequestDto) {
        val artist = tattooArtistService.findById(artistId)
        val saved = supportMessageRepository.save(request.toEntity(artist))
        eventPublisher.publishEvent(SupportMessageReceivedEmailRequested(saved))
        eventPublisher.publishEvent(SupportMessageConfirmationEmailRequested(artist.email, artist.artistName))
    }
}
