package com.inkvite.inkviteback.auth.controller

import com.inkvite.inkviteback.auth.service.AuthService
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
        authService.register(request.email, request.password)

    @GetMapping("/verify")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun verify(@RequestParam token: String) =
        authService.verify(token)
}