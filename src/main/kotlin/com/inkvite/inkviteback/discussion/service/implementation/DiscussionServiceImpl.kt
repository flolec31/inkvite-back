package com.inkvite.inkviteback.discussion.service.implementation

import com.inkvite.inkviteback.appointment.service.AppointmentAccessService
import com.inkvite.inkviteback.discussion.dto.MessageResponseDto
import com.inkvite.inkviteback.discussion.entity.Message
import com.inkvite.inkviteback.discussion.entity.MessageSender
import com.inkvite.inkviteback.discussion.repository.MessageRepository
import com.inkvite.inkviteback.discussion.service.DiscussionService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class DiscussionServiceImpl(
    private val appointmentAccessService: AppointmentAccessService,
    private val messageRepository: MessageRepository,
) : DiscussionService {

    override fun getMessages(artistId: UUID, appointmentId: UUID): List<MessageResponseDto> {
        appointmentAccessService.findOwnedAppointment(artistId, appointmentId)
        return messageRepository.findByAppointmentIdOrderBySentAtAsc(appointmentId)
            .map { MessageResponseDto(it) }
    }

    @Transactional
    override fun postMessage(artistId: UUID, appointmentId: UUID, content: String): MessageResponseDto {
        val appointment = appointmentAccessService.findOwnedAppointment(artistId, appointmentId)
        val message = messageRepository.save(
            Message(appointment = appointment, sender = MessageSender.ARTIST, content = content)
        )
        return MessageResponseDto(message)
    }
}
