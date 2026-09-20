package com.inkvite.inkviteback.discussion.controller

import com.inkvite.inkviteback.discussion.dto.MessageResponseDto
import com.inkvite.inkviteback.discussion.service.DiscussionClientService
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/client/appointment")
class DiscussionClientController(
    private val discussionClientService: DiscussionClientService,
) {

    @GetMapping("/{appointmentId}/messages")
    fun getMessages(
        authentication: JwtAuthenticationToken,
        @PathVariable appointmentId: UUID,
    ): List<MessageResponseDto> {
        val clientId = UUID.fromString(authentication.token.subject)
        return discussionClientService.getMessages(clientId, appointmentId)
    }
}
