package com.inkvite.inkviteback.appointment.service.implementation

import com.inkvite.inkviteback.appointment.dto.AppointmentDetailsResponseDto
import com.inkvite.inkviteback.appointment.dto.AppointmentItemResponseDto
import com.inkvite.inkviteback.appointment.dto.ReferenceDetailsResponseDto
import com.inkvite.inkviteback.appointment.entity.Appointment
import com.inkvite.inkviteback.appointment.exception.AppointmentAlreadyNewException
import com.inkvite.inkviteback.appointment.exception.AppointmentArchiveStateException
import com.inkvite.inkviteback.appointment.exception.AppointmentBelongsToAnotherArtistException
import com.inkvite.inkviteback.appointment.exception.AppointmentNotFoundException
import com.inkvite.inkviteback.appointment.exception.CannotMarkArchivedAppointmentAsNewException
import com.inkvite.inkviteback.appointment.repository.AppointmentRepository
import com.inkvite.inkviteback.appointment.repository.ReferenceRepository
import com.inkvite.inkviteback.appointment.service.AppointmentManagementService
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
    private val referenceRepository: ReferenceRepository
) : AppointmentManagementService {

    override fun getAppointmentsOf(artistId: UUID, pageable: Pageable): Page<AppointmentItemResponseDto> =
        appointmentRepository.findByArtistIdAndVerifiedAtNotNull(artistId, pageable)
            .map { AppointmentItemResponseDto(it) }

    @Transactional
    override fun getAppointmentDetails(
        artistId: UUID,
        appointmentId: UUID
    ): AppointmentDetailsResponseDto {
        val appointment = findOwnedAppointment(artistId, appointmentId)

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
        val appointment = findOwnedAppointment(artistId, appointmentId)
        if (appointment.archived) throw CannotMarkArchivedAppointmentAsNewException()
        if (appointment.new) throw AppointmentAlreadyNewException()

        appointment.new = true
        appointmentRepository.save(appointment)
    }

    private fun setArchived(artistId: UUID, appointmentId: UUID, archived: Boolean) {
        val appointment = findOwnedAppointment(artistId, appointmentId)
        if (appointment.archived == archived) throw AppointmentArchiveStateException(archived)

        appointment.archived = archived
        appointmentRepository.save(appointment)
    }

    private fun findOwnedAppointment(artistId: UUID, appointmentId: UUID): Appointment {
        val appointment = appointmentRepository.findByIdAndVerifiedAtNotNull(appointmentId)
            .orElseThrow { AppointmentNotFoundException() }
        if (appointment.artist.id != artistId) throw AppointmentBelongsToAnotherArtistException()
        return appointment
    }

    private fun toDetailsResponse(appointment: Appointment): AppointmentDetailsResponseDto {
        val references = referenceRepository.findByAppointmentId(appointment.id)
        val referencesDto = references.map {
            val url = storageService.getSignedUrl(it.key)
            ReferenceDetailsResponseDto(it, url)
        }
        return AppointmentDetailsResponseDto(appointment, referencesDto)
    }
}
