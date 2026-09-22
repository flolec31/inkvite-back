package com.inkvite.inkviteback.discussion.controller

import com.inkvite.inkviteback.discussion.dto.MessageRequestDto
import com.inkvite.inkviteback.discussion.dto.MessageResponseDto
import com.inkvite.inkviteback.discussion.service.DiscussionService
import com.inkvite.inkviteback.storage.dto.ImageUploadResponseDto
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@RestController
@RequestMapping("/client/appointment")
class DiscussionClientController(
    @Qualifier("discussionClientServiceImpl") private val discussionClientService: DiscussionService,
) {

    @GetMapping("/{appointmentId}/messages")
    fun getMessages(
        authentication: JwtAuthenticationToken,
        @PathVariable appointmentId: UUID,
    ): List<MessageResponseDto> {
        val clientId = UUID.fromString(authentication.token.subject)
        return discussionClientService.getMessages(clientId, appointmentId)
    }

    @PostMapping("/{appointmentId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    fun postMessage(
        authentication: JwtAuthenticationToken,
        @PathVariable appointmentId: UUID,
        @Valid @RequestBody request: MessageRequestDto,
    ): MessageResponseDto {
        val clientId = UUID.fromString(authentication.token.subject)
        return discussionClientService.postMessage(clientId, appointmentId, request.content, request.imageKey)
    }

    @PostMapping("/{appointmentId}/messages/image", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    fun uploadMessageImage(
        authentication: JwtAuthenticationToken,
        @PathVariable appointmentId: UUID,
        @RequestParam("image") image: MultipartFile,
    ): ImageUploadResponseDto {
        val clientId = UUID.fromString(authentication.token.subject)
        return discussionClientService.uploadMessageImage(clientId, appointmentId, image)
    }
}
