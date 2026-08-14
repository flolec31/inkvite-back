package com.inkvite.inkviteback.discussion.controller

import com.inkvite.inkviteback.discussion.dto.MessageRequestDto
import com.inkvite.inkviteback.discussion.dto.MessageResponseDto
import com.inkvite.inkviteback.discussion.service.DiscussionService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/appointment")
class DiscussionController(
    private val discussionService: DiscussionService,
) {

    @GetMapping("/{appointmentId}/messages")
    fun getMessages(
        authentication: JwtAuthenticationToken,
        @PathVariable appointmentId: UUID,
    ): List<MessageResponseDto> {
        val artistId = UUID.fromString(authentication.token.subject)
        return discussionService.getMessages(artistId, appointmentId)
    }

    @PostMapping("/{appointmentId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    fun postMessage(
        authentication: JwtAuthenticationToken,
        @PathVariable appointmentId: UUID,
        @Valid @RequestBody request: MessageRequestDto,
    ): MessageResponseDto {
        val artistId = UUID.fromString(authentication.token.subject)
        return discussionService.postMessage(artistId, appointmentId, request.content)
    }
}
