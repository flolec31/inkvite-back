package com.inkvite.inkviteback.artist.service

import java.util.UUID

interface TattooArtistService {
    fun register(email: String, encodedPassword: String): UUID
    fun activate(artistId: UUID)
}