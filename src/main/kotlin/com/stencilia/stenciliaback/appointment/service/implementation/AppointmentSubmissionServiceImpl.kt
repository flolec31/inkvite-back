package com.stencilia.stenciliaback.appointment.service.implementation

import com.stencilia.stenciliaback.appointment.dto.AppointmentFormRequestDto
import com.stencilia.stenciliaback.appointment.event.AppointmentRequestConfirmationEmailRequested
import com.stencilia.stenciliaback.appointment.event.AppointmentLinksRequested
import com.stencilia.stenciliaback.appointment.event.AppointmentNotificationEmailRequested
import com.stencilia.stenciliaback.appointment.event.AppointmentVerificationEmailRequested
import com.stencilia.stenciliaback.appointment.exception.AppointmentNotFoundException
import com.stencilia.stenciliaback.appointment.repository.AppointmentRepository
import com.stencilia.stenciliaback.appointment.repository.ReferenceRepository
import com.stencilia.stenciliaback.appointment.service.AppointmentSubmissionService
import com.stencilia.stenciliaback.artist.service.TattooArtistService
import com.stencilia.stenciliaback.client.service.TattooClientService
import com.stencilia.stenciliaback.storage.dto.ImageUploadResponseDto
import com.stencilia.stenciliaback.storage.service.ImageUploadService
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
        eventPublisher.publishEvent(AppointmentRequestConfirmationEmailRequested(appointment))
    }

    override fun requestLinks(slug: String, email: String) {
        val artist = tattooArtistService.findBySlug(slug)
        val appointments =
            appointmentRepository.findByArtistIdAndClientEmailIgnoreCaseAndVerifiedAtNotNullOrderBySubmittedAtDesc(
                artist.id,
                email
            )
        if (appointments.isNotEmpty()) {
            eventPublisher.publishEvent(AppointmentLinksRequested(appointments))
        }
    }
}
