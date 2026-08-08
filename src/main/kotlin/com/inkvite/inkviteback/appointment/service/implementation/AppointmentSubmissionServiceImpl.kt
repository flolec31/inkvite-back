package com.inkvite.inkviteback.appointment.service.implementation

import com.inkvite.inkviteback.appointment.dto.AppointmentFormRequestDto
import com.inkvite.inkviteback.appointment.event.AppointmentNotificationEmailRequested
import com.inkvite.inkviteback.appointment.event.AppointmentVerificationEmailRequested
import com.inkvite.inkviteback.appointment.exception.AppointmentNotFoundException
import com.inkvite.inkviteback.appointment.repository.AppointmentRepository
import com.inkvite.inkviteback.appointment.repository.ReferenceRepository
import com.inkvite.inkviteback.appointment.service.AppointmentSubmissionService
import com.inkvite.inkviteback.artist.service.TattooArtistService
import com.inkvite.inkviteback.client.service.TattooClientService
import com.inkvite.inkviteback.storage.dto.ImageUploadResponseDto
import com.inkvite.inkviteback.storage.service.ImageUploadService
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.Instant
import java.util.*

@Service
@Transactional(readOnly = true)
class AppointmentSubmissionServiceImpl(
    private val eventPublisher: ApplicationEventPublisher,
    private val tattooArtistService: TattooArtistService,
    private val tattooClientService: TattooClientService,
    private val imageUploadService: ImageUploadService,
    private val appointmentRepository: AppointmentRepository,
    private val referenceRepository: ReferenceRepository
) : AppointmentSubmissionService {

    @Transactional
    override fun save(appointmentDto: AppointmentFormRequestDto, slug: String) {
        val artist = tattooArtistService.findBySlug(slug)
        val client = tattooClientService.findOrCreate(
            appointmentDto.email,
            appointmentDto.firstName,
            appointmentDto.lastName
        )
        val appointment = appointmentRepository.save(appointmentDto.toEntity(artist, client))
        referenceRepository.saveAll(appointmentDto.references.map { it.toEntity(appointment) })
        eventPublisher.publishEvent(AppointmentVerificationEmailRequested(appointment))
    }

    override fun uploadReference(
        slug: String,
        photo: MultipartFile
    ): ImageUploadResponseDto {
        val artist = tattooArtistService.findBySlug(slug)
        return imageUploadService.uploadReference(artist.id, photo)
    }

    @Transactional
    override fun verify(appointmentId: UUID) {
        var appointment = appointmentRepository.findById(appointmentId).orElseThrow { AppointmentNotFoundException() }
        appointment.verifiedAt = Instant.now()
        appointment = appointmentRepository.save(appointment)
        eventPublisher.publishEvent(AppointmentNotificationEmailRequested(appointment))
    }
}
