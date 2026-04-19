package com.inkvite.inkviteback.auth.controller

import com.inkvite.inkviteback.auth.service.AuthService
import com.inkvite.inkviteback.auth.dto.LoginRequestDto
import com.inkvite.inkviteback.auth.dto.LoginResponseDto
import com.inkvite.inkviteback.auth.dto.LogoutRequestDto
import com.inkvite.inkviteback.auth.dto.RefreshRequestDto
import com.inkvite.inkviteback.auth.dto.RegisterRequestDto
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun register(@Valid @RequestBody request: RegisterRequestDto) =
        authService.register(request.email, request.password, request.artistName, request.slug)

    @GetMapping("/verify")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun verify(@RequestParam token: String) =
        authService.verify(token)

    @PostMapping("/resend-verification")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun resendVerification(@RequestParam email: String) =
        authService.resendVerification(email)

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequestDto): LoginResponseDto =
        authService.login(request.email, request.password)

    @PostMapping("/refresh")
    fun refresh(@Valid @RequestBody request: RefreshRequestDto): LoginResponseDto =
        authService.refresh(request.refreshToken)

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun logout(@Valid @RequestBody request: LogoutRequestDto) =
        authService.logout(request.refreshToken)
}