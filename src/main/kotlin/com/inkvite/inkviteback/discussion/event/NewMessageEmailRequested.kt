package com.inkvite.inkviteback.discussion.event

import com.inkvite.inkviteback.appointment.entity.Appointment

data class NewMessageEmailRequested(val appointment: Appointment)
