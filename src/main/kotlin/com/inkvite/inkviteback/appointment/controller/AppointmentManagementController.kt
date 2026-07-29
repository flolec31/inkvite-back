package com.inkvite.inkviteback.appointment.controller

import com.inkvite.inkviteback.appointment.dto.AppointmentDetailsResponseDto
import com.inkvite.inkviteback.appointment.dto.AppointmentItemResponseDto
import com.inkvite.inkviteback.appointment.service.AppointmentManagementService
import com.inkvite.inkviteback.common.dto.PagedResponseDto
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/appointment")
class AppointmentManagementController(
    private val appointmentManagementService: AppointmentManagementService
) {

    @GetMapping
    fun getAppointmentsList(
        authentication: JwtAuthenticationToken,
        @PageableDefault(size = 20, sort = ["verifiedAt"], direction = Sort.Direction.DESC) pageable: Pageable
    ): PagedResponseDto<AppointmentItemResponseDto> {
        val artistId = UUID.fromString(authentication.token.subject)
        val appointments = appointmentManagementService.getAppointmentsOf(artistId, pageable)
        return PagedResponseDto(
            content = appointments.content,
            total = appointments.totalElements,
            page = appointments.number,
            pageCount = appointments.totalPages
        )
    }

    @GetMapping("/{appointmentId}")
    fun getAppointment(
        authentication: JwtAuthenticationToken,
        @PathVariable appointmentId: UUID,
    ): AppointmentDetailsResponseDto {
        val artistId = UUID.fromString(authentication.token.subject)
        return appointmentManagementService.getAppointmentDetails(artistId, appointmentId)
    }

    @PostMapping("/{appointmentId}/archive")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun archiveAppointment(
        authentication: JwtAuthenticationToken,
        @PathVariable appointmentId: UUID,
    ) {
        val artistId = UUID.fromString(authentication.token.subject)
        appointmentManagementService.archiveAppointment(artistId, appointmentId)
    }

    @PostMapping("/{appointmentId}/unarchive")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun unarchiveAppointment(
        authentication: JwtAuthenticationToken,
        @PathVariable appointmentId: UUID,
    ) {
        val artistId = UUID.fromString(authentication.token.subject)
        appointmentManagementService.unarchiveAppointment(artistId, appointmentId)
    }
}
