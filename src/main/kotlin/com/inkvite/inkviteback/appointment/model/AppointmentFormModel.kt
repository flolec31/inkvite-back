package com.inkvite.inkviteback.appointment.model

data class AppointmentFormModel(
    val artistSlug: String,
    val clientEmail: String,
    val clientFirstName: String,
    val clientLastName: String,
    val tattooDescription: String,
    val tattooPlacement: String,
    val tattooSize: String,
    val firstTattoo: Boolean,
    val coverUp: Boolean,
    val references: List<CommentedReferenceModel>
)