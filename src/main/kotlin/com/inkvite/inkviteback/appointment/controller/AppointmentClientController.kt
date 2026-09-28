package com.inkvite.inkviteback.appointment.controller

import com.inkvite.inkviteback.appointment.dto.ClientAppointmentDetailsResponseDto
import com.inkvite.inkviteback.appointment.service.AppointmentClientService
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/client/appointment")
class AppointmentClientController(
    private val appointmentClientService: AppointmentClientService
) {

    @GetMapping("/{appointmentId}")
    fun getAppointment(
        authentication: JwtAuthenticationToken,
        @PathVariable appointmentId: UUID,
    ): ClientAppointmentDetailsResponseDto {
        val clientId = UUID.fromString(authentication.token.subject)
        return appointmentClientService.getAppointmentDetails(clientId, appointmentId)
    }
}
