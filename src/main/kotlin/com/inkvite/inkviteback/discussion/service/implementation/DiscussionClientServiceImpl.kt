package com.inkvite.inkviteback.discussion.service.implementation

import com.inkvite.inkviteback.appointment.service.AppointmentAccessService
import com.inkvite.inkviteback.discussion.dto.MessageResponseDto
import com.inkvite.inkviteback.discussion.repository.MessageRepository
import com.inkvite.inkviteback.discussion.service.DiscussionClientService
import com.inkvite.inkviteback.storage.service.StorageService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class DiscussionClientServiceImpl(
    private val appointmentAccessService: AppointmentAccessService,
    private val messageRepository: MessageRepository,
    private val storageService: StorageService,
) : DiscussionClientService {

    override fun getMessages(clientId: UUID, appointmentId: UUID): List<MessageResponseDto> {
        appointmentAccessService.findAppointmentForClient(clientId, appointmentId)
        return messageRepository.findByAppointmentIdOrderBySentAtAsc(appointmentId)
            .map { MessageResponseDto(it, it.imageKey?.let(storageService::getSignedUrl)) }
    }
}
