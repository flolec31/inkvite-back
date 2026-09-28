package com.inkvite.inkviteback.auth.client.controller

import com.inkvite.inkviteback.auth.client.dto.ClientRefreshRequestDto
import com.inkvite.inkviteback.auth.client.dto.RequestCodeRequestDto
import com.inkvite.inkviteback.auth.client.dto.VerifyCodeRequestDto
import com.inkvite.inkviteback.auth.client.service.ClientAuthService
import com.inkvite.inkviteback.auth.dto.LoginResponseDto
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth/client")
class ClientAuthController(
    private val clientAuthService: ClientAuthService,
) {
    @PostMapping("/request-code")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun requestCode(@Valid @RequestBody request: RequestCodeRequestDto) =
        clientAuthService.requestCode(request.appointmentId)

    @PostMapping("/verify-code")
    fun verifyCode(@Valid @RequestBody request: VerifyCodeRequestDto): LoginResponseDto =
        clientAuthService.verifyCode(request.appointmentId, request.code)

    @PostMapping("/refresh")
    fun refresh(@Valid @RequestBody request: ClientRefreshRequestDto): LoginResponseDto =
        clientAuthService.refresh(request.refreshToken)
}
