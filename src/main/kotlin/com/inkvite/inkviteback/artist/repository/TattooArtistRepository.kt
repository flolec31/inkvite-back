package com.inkvite.inkviteback.artist.repository

import com.inkvite.inkviteback.artist.entity.TattooArtist
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface TattooArtistRepository : JpaRepository<TattooArtist, UUID> {
    fun existsByEmail(email: String): Boolean
}