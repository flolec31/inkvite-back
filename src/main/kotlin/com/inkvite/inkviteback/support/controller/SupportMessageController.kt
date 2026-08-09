package com.inkvite.inkviteback.support.controller

import com.inkvite.inkviteback.storage.dto.ImageUploadResponseDto
import com.inkvite.inkviteback.support.dto.SupportMessageRequestDto
import com.inkvite.inkviteback.support.service.SupportMessageService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@RestController
@RequestMapping("/support")
class SupportMessageController(
    private val supportMessageService: SupportMessageService
) {

    @PostMapping("/screenshot", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadScreenshot(
        @RequestParam("image") photo: MultipartFile,
        authentication: JwtAuthenticationToken
    ): ImageUploadResponseDto {
        val artistId = UUID.fromString(authentication.token.subject)
        return supportMessageService.uploadScreenshot(artistId, photo)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun submit(
        @Valid @RequestBody request: SupportMessageRequestDto,
        authentication: JwtAuthenticationToken
    ) {
        val artistId = UUID.fromString(authentication.token.subject)
        supportMessageService.submit(artistId, request)
    }
}
