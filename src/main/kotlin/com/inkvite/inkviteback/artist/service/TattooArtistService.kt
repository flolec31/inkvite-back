package com.inkvite.inkviteback.artist.service

import java.util.UUID

interface TattooArtistService {
    fun register(id: UUID, email: String, encodedPassword: String)
    fun activate(artistId: UUID)
}