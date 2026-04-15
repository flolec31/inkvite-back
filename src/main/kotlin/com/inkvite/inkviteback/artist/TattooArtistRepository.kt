package com.inkvite.inkviteback.artist

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface TattooArtistRepository : JpaRepository<TattooArtist, UUID> {
    fun existsByEmail(email: String): Boolean
}