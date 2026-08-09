package com.inkvite.inkviteback.appointment.exception

class AppointmentNotFoundException : RuntimeException("Appointment not found")

class AppointmentBelongsToAnotherArtistException : RuntimeException("The requested appointment belongs to another artist")

class AppointmentArchiveStateException(archived: Boolean) :
    RuntimeException(if (archived) "Appointment is already archived" else "Appointment is not archived")
