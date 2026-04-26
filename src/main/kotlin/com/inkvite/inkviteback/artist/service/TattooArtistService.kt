package com.inkvite.inkviteback.artist.service

import com.inkvite.inkviteback.artist.dto.ProfileResponseDto
import com.inkvite.inkviteback.artist.entity.TattooArtist
import org.springframework.web.multipart.MultipartFile
import java.util.*

interface TattooArtistService {
    fun register(email: String, encodedPassword: String, artistName: String, slug: String): UUID
    fun activate(artistId: UUID)
    fun findUnactivatedByEmail(email: String): UUID?
    fun findByEmail(email: String): TattooArtist?
    fun findBySlug(slug: String): TattooArtist
    fun existsBySlug(slug: String): Boolean
    fun existsBySlugAndIdNot(slug: String, artistId: UUID): Boolean
    fun findById(artistId: UUID): TattooArtist
    fun updateProfile(artistId: UUID, artistName: String?, slug: String?): ProfileResponseDto
    fun updatePhoto(artistId: UUID, photo: MultipartFile): String
}
