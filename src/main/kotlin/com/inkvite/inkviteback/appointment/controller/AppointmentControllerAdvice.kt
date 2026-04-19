package com.inkvite.inkviteback.appointment.controller

import com.inkvite.inkviteback.appointment.exception.AppointmentFormNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class AppointmentControllerAdvice {

    @ExceptionHandler(AppointmentFormNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleAppointmentFormNotFound(e: AppointmentFormNotFoundException) =
        mapOf("error" to e.message)
}