package com.inkvite.inkviteback.appointment.service.implementation

import com.inkvite.inkviteback.appointment.dto.AppointmentDetailsResponseDto
import com.inkvite.inkviteback.appointment.dto.AppointmentItemResponseDto
import com.inkvite.inkviteback.appointment.dto.ReferenceDetailsResponseDto
import com.inkvite.inkviteback.appointment.entity.Appointment
import com.inkvite.inkviteback.appointment.exception.AppointmentAlreadyNewException
import com.inkvite.inkviteback.appointment.exception.AppointmentArchiveStateException
import com.inkvite.inkviteback.appointment.exception.CannotMarkArchivedAppointmentAsNewException
import com.inkvite.inkviteback.appointment.repository.AppointmentRepository
import com.inkvite.inkviteback.appointment.repository.ReferenceRepository
import com.inkvite.inkviteback.appointment.service.AppointmentAccessService
import com.inkvite.inkviteback.appointment.service.AppointmentManagementService
import com.inkvite.inkviteback.discussion.entity.MessageSender
import com.inkvite.inkviteback.discussion.repository.MessageRepository
import com.inkvite.inkviteback.storage.service.StorageService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional(readOnly = true)
class AppointmentManagementServiceImpl(
    private val storageService: StorageService,
    private val appointmentRepository: AppointmentRepository,
    private val referenceRepository: ReferenceRepository,
    private val appointmentAccessService: AppointmentAccessService,
    private val messageRepository: MessageRepository,
) : AppointmentManagementService {

    override fun getAppointmentsOf(artistId: UUID, pageable: Pageable): Page<AppointmentItemResponseDto> {
        val page = appointmentRepository.findByArtistIdAndVerifiedAtNotNull(artistId, pageable)
        val ids = page.content.map { it.id }
        /* Ids of appointment with unread messages */
        val unreadIds =
            if (ids.isEmpty()) emptySet()
            else messageRepository.findAppointmentIdsWithUnreadFrom(ids, MessageSender.CLIENT)
        return page.map { AppointmentItemResponseDto(it, unreadMessages = it.id in unreadIds) }
    }

    @Transactional
    override fun getAppointmentDetails(
        artistId: UUID,
        appointmentId: UUID
    ): AppointmentDetailsResponseDto {
        val appointment = appointmentAccessService.findOwnedAppointment(artistId, appointmentId)

        if (appointment.new) {
            appointment.new = false
            appointmentRepository.save(appointment)
        }

        return toDetailsResponse(appointment)
    }

    @Transactional
    override fun archiveAppointment(artistId: UUID, appointmentId: UUID) =
        setArchived(artistId, appointmentId, archived = true)

    @Transactional
    override fun unarchiveAppointment(artistId: UUID, appointmentId: UUID) =
        setArchived(artistId, appointmentId, archived = false)

    @Transactional
    override fun markAppointmentAsNew(artistId: UUID, appointmentId: UUID) {
        val appointment = appointmentAccessService.findOwnedAppointment(artistId, appointmentId)
        if (appointment.archived) throw CannotMarkArchivedAppointmentAsNewException()
        if (appointment.new) throw AppointmentAlreadyNewException()

        appointment.new = true
        appointmentRepository.save(appointment)
    }

    private fun setArchived(artistId: UUID, appointmentId: UUID, archived: Boolean) {
        val appointment = appointmentAccessService.findOwnedAppointment(artistId, appointmentId)
        if (appointment.archived == archived) throw AppointmentArchiveStateException(archived)

        appointment.archived = archived
        appointmentRepository.save(appointment)
    }

    private fun toDetailsResponse(appointment: Appointment): AppointmentDetailsResponseDto {
        val references = referenceRepository.findByAppointmentId(appointment.id)
        val referencesDto = references.map {
            val url = storageService.getSignedUrl(it.key)
            ReferenceDetailsResponseDto(it, url)
        }
        val unreadMessages =
            messageRepository.existsByAppointmentIdAndSenderAndReadAtIsNull(appointment.id, MessageSender.CLIENT)
        return AppointmentDetailsResponseDto(appointment, referencesDto, unreadMessages)
    }
}
