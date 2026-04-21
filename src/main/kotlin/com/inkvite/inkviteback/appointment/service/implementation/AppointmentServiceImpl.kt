package com.inkvite.inkviteback.appointment.service.implementation

import com.inkvite.inkviteback.appointment.entity.AppointmentForm
import com.inkvite.inkviteback.appointment.event.AppointmentVerificationEmailRequested
import com.inkvite.inkviteback.appointment.exception.AppointmentFormNotFoundException
import com.inkvite.inkviteback.appointment.model.AppointmentFormModel
import com.inkvite.inkviteback.appointment.repository.AppointmentFormRepository
import com.inkvite.inkviteback.appointment.repository.ReferenceRepository
import com.inkvite.inkviteback.appointment.service.AppointmentService
import com.inkvite.inkviteback.artist.service.TattooArtistService
import com.inkvite.inkviteback.client.service.TattooClientService
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
@Transactional(readOnly = true)
class AppointmentServiceImpl(
    private val eventPublisher: ApplicationEventPublisher,
    private val tattooArtistService: TattooArtistService,
    private val tattooClientService: TattooClientService,
    private val appointmentFormRepository: AppointmentFormRepository,
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
        val form = appointmentFormRepository.save(AppointmentForm(appointmentFormModel, artist, client))
        referenceRepository.saveAll(appointmentFormModel.references.map { it.toEntity(form) })
        eventPublisher.publishEvent(AppointmentVerificationEmailRequested(appointmentFormModel.clientEmail, form.id))
    }

    @Transactional
    override fun verify(formId: UUID) {
        val form = appointmentFormRepository.findById(formId).orElseThrow { AppointmentFormNotFoundException() }
        form.verifiedAt = Instant.now()
        appointmentFormRepository.save(form)
    }
}
