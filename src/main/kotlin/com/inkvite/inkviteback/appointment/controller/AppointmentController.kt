package com.inkvite.inkviteback.appointment.controller

import com.inkvite.inkviteback.appointment.dto.AppointmentFormRequestDto
import com.inkvite.inkviteback.appointment.service.AppointmentService
import jakarta.validation.Valid
import jakarta.validation.constraints.Pattern
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

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
    ) = appointmentService.save(form.toModel(slug))

    @GetMapping("/verify")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun verify(@RequestParam formId: UUID) =
        appointmentService.verify(formId)

}