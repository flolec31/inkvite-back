package com.inkvite.inkviteback.appointment.service.implementation

import com.inkvite.inkviteback.appointment.dto.AppointmentDetailsResponseDto
import com.inkvite.inkviteback.appointment.dto.ReferenceDetailsResponseDto
import com.inkvite.inkviteback.appointment.entity.Appointment
import com.inkvite.inkviteback.appointment.event.AppointmentVerificationEmailRequested
import com.inkvite.inkviteback.appointment.exception.*
import com.inkvite.inkviteback.appointment.model.AppointmentFormModel
import com.inkvite.inkviteback.appointment.model.AppointmentItemModel
import com.inkvite.inkviteback.appointment.model.UploadedReferenceModel
import com.inkvite.inkviteback.appointment.repository.AppointmentRepository
import com.inkvite.inkviteback.appointment.repository.ReferenceRepository
import com.inkvite.inkviteback.appointment.service.AppointmentService
import com.inkvite.inkviteback.artist.service.TattooArtistService
import com.inkvite.inkviteback.client.service.TattooClientService
import com.inkvite.inkviteback.storage.service.StorageService
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.Instant
import java.util.*

@Service
@Transactional(readOnly = true)
class AppointmentServiceImpl(
    private val eventPublisher: ApplicationEventPublisher,
    private val tattooArtistService: TattooArtistService,
    private val tattooClientService: TattooClientService,
    private val storageService: StorageService,
    private val appointmentRepository: AppointmentRepository,
    private val referenceRepository: ReferenceRepository
) : AppointmentService {

    @Transactional
    override fun save(appointmentFormModel: AppointmentFormModel) {
        val artist = tattooArtistService.findBySlug(appointmentFormModel.artistSlug)
        val client = tattooClientService.findOrCreate(
            appointmentFormModel.clientEmail,
            appointmentFormModel.clientFirstName,
            appointmentFormModel.clientLastName
        )
        val form = appointmentRepository.save(Appointment(appointmentFormModel, artist, client))
        referenceRepository.saveAll(appointmentFormModel.references.map { it.toEntity(form) })
        eventPublisher.publishEvent(AppointmentVerificationEmailRequested(appointmentFormModel.clientEmail, form.id))
    }

    override fun uploadReference(
        slug: String,
        photo: MultipartFile
    ): UploadedReferenceModel {
        val allowedTypes = setOf("image/jpeg", "image/png", "image/webp")
        if (photo.contentType !in allowedTypes) throw InvalidReferenceContentTypeException()
        if (photo.size > 5 * 1024 * 1024) throw ReferenceTooLargeException()

        val artist = tattooArtistService.findBySlug(slug)
        val photoKey = "references/${artist.id}/${UUID.randomUUID()}"
        val url = try {
            storageService.upload(photoKey, photo.bytes, photo.contentType!!)
        } catch (e: Exception) {
            throw ReferenceUploadFailedException(e)
        }
        return UploadedReferenceModel(key = photoKey, url = url)
    }

    @Transactional
    override fun verify(formId: UUID) {
        val form = appointmentRepository.findById(formId).orElseThrow { AppointmentNotFoundException() }
        form.verifiedAt = Instant.now()
        appointmentRepository.save(form)
    }

    override fun getAppointmentsOf(artistId: UUID, pageable: Pageable): Page<AppointmentItemModel> =
        appointmentRepository.findByArtistIdAndVerifiedAtNotNull(artistId, pageable)
            .map { it.toModel() }

    override fun getAppointmentDetails(
        artistId: UUID,
        appointmentId: UUID
    ): AppointmentDetailsResponseDto {
        val appointment = appointmentRepository.findByIdAndVerifiedAtNotNull(appointmentId)
            .orElseThrow { AppointmentNotFoundException() }
        if (appointment.artist.id != artistId) throw AppointmentBelongsToAnotherArtistException()

        if (appointment.new) {
            appointment.new = false
            appointmentRepository.save(appointment)
        }

        val references = referenceRepository.findByAppointmentId(appointmentId)
        val referencesDto = references.map {
            val url = storageService.getSignedUrl(it.key)
            ReferenceDetailsResponseDto(it, url)
        }
        return AppointmentDetailsResponseDto(appointment, referencesDto)
    }
}
