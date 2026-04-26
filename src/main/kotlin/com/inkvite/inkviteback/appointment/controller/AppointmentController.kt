package com.inkvite.inkviteback.appointment.controller

import com.inkvite.inkviteback.appointment.dto.AppointmentDetailsResponseDto
import com.inkvite.inkviteback.appointment.dto.AppointmentFormRequestDto
import com.inkvite.inkviteback.appointment.dto.AppointmentItemResponseDto
import com.inkvite.inkviteback.appointment.dto.ReferenceUploadResponseDto
import com.inkvite.inkviteback.appointment.service.AppointmentService
import com.inkvite.inkviteback.common.dto.PagedResponseDto
import jakarta.validation.Valid
import jakarta.validation.constraints.Pattern
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.*

@RestController
@RequestMapping("/appointment")
@Validated
class AppointmentController(
    private val appointmentService: AppointmentService
) {

    @PostMapping("/{slug}")
    @ResponseStatus(HttpStatus.CREATED)
    fun newAppointmentForm(
        @PathVariable @Pattern(regexp = "^[a-z0-9][a-z0-9-]{1,28}[a-z0-9]$") slug: String,
        @Valid @RequestBody form: AppointmentFormRequestDto
    ) = appointmentService.save(form, slug)

    @PostMapping("/{slug}/reference", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    fun uploadReference(
        @PathVariable @Pattern(regexp = "^[a-z0-9][a-z0-9-]{1,28}[a-z0-9]$") slug: String,
        @RequestParam("photo") photo: MultipartFile
    ): ReferenceUploadResponseDto = appointmentService.uploadReference(slug, photo)

    @GetMapping("/verify")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun verify(@RequestParam formId: UUID) =
        appointmentService.verify(formId)

    @GetMapping("/")
    fun getAppointmentsList(
        authentication: JwtAuthenticationToken,
        @PageableDefault(size = 20, sort = ["verifiedAt"], direction = Sort.Direction.DESC) pageable: Pageable
    ): PagedResponseDto<AppointmentItemResponseDto> {
        val artistId = UUID.fromString(authentication.token.subject)
        val appointments = appointmentService.getAppointmentsOf(artistId, pageable)
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
        return appointmentService.getAppointmentDetails(artistId, appointmentId)
    }

}