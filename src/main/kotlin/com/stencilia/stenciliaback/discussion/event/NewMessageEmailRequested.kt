package com.stencilia.stenciliaback.discussion.event

import com.stencilia.stenciliaback.appointment.entity.Appointment

data class NewMessageEmailRequested(val appointment: Appointment)
