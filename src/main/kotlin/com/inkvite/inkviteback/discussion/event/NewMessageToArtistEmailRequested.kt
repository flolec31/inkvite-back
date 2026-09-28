package com.inkvite.inkviteback.discussion.event

import com.inkvite.inkviteback.appointment.entity.Appointment

data class NewMessageToArtistEmailRequested(val appointment: Appointment)
