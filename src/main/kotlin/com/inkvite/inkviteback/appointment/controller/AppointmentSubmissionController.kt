package com.inkvite.inkviteback.appointment.controller

import com.inkvite.inkviteback.appointment.dto.AppointmentFormRequestDto
import com.inkvite.inkviteback.appointment.dto.ReferenceUploadResponseDto
import com.inkvite.inkviteback.appointment.service.AppointmentSubmissionService
import jakarta.validation.Valid
import jakarta.validation.constraints.Pattern
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.*

@RestController
@RequestMapping("/appointment")
@Validated
class AppointmentSubmissionController(
    private val appointmentSubmissionService: AppointmentSubmissionService
) {

    @PostMapping("/{slug}")
    @ResponseStatus(HttpStatus.CREATED)
    fun newAppointmentForm(
        @PathVariable @Pattern(regexp = "^[a-z0-9][a-z0-9-]{1,28}[a-z0-9]$") slug: String,
        @Valid @RequestBody form: AppointmentFormRequestDto
    ) = appointmentSubmissionService.save(form, slug)

    @PostMapping("/{slug}/reference", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    fun uploadReference(
        @PathVariable @Pattern(regexp = "^[a-z0-9][a-z0-9-]{1,28}[a-z0-9]$") slug: String,
        @RequestParam("image") photo: MultipartFile
    ): ReferenceUploadResponseDto = appointmentSubmissionService.uploadReference(slug, photo)

    @GetMapping("/verify")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun verify(@RequestParam appointmentId: UUID) =
        appointmentSubmissionService.verify(appointmentId)
}
