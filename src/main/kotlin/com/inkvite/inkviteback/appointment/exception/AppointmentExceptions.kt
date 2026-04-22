package com.inkvite.inkviteback.appointment.exception

class AppointmentFormNotFoundException : RuntimeException("Appointment form not found")

class InvalidReferenceContentTypeException : RuntimeException("Reference photo must be a JPEG, PNG, or WebP image")

class ReferenceTooLargeException : RuntimeException("Reference photo must not exceed 5 MB")

class ReferenceUploadFailedException(cause: Throwable) : RuntimeException("Failed to upload reference photo", cause)