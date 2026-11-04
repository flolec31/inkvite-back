package com.stencilia.stenciliaback.discussion.event

import com.stencilia.stenciliaback.appointment.entity.Appointment

data class NewMessageToArtistEmailRequested(val appointment: Appointment)
