package com.inkvite.inkviteback.artist.service

import com.inkvite.inkviteback.artist.entity.TattooArtist
import java.util.UUID

interface TattooArtistService {
    fun register(email: String, encodedPassword: String): UUID
    fun activate(artistId: UUID)
    fun findUnactivatedByEmail(email: String): UUID?
    fun findByEmail(email: String): TattooArtist?
}