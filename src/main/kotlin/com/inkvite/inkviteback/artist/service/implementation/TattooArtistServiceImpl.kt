package com.inkvite.inkviteback.artist.service.implementation

import com.inkvite.inkviteback.artist.dto.ProfileResponseDto
import com.inkvite.inkviteback.artist.entity.TattooArtist
import com.inkvite.inkviteback.artist.exception.TattooArtistAlreadyExistsException
import com.inkvite.inkviteback.artist.exception.TattooArtistNotFoundException
import com.inkvite.inkviteback.artist.repository.TattooArtistRepository
import com.inkvite.inkviteback.artist.service.TattooArtistService
import com.inkvite.inkviteback.storage.service.StorageService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.Instant
import java.util.*

@Service
@Transactional(readOnly = true)
class TattooArtistServiceImpl(
    private val repository: TattooArtistRepository,
    private val storageService: StorageService
) : TattooArtistService {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    override fun register(email: String, encodedPassword: String, artistName: String, slug: String): UUID {
        if (repository.existsByEmail(email)) throw TattooArtistAlreadyExistsException()
        val id = UUID.randomUUID()
        repository.save(
            TattooArtist(
                id = id,
                email = email,
                password = encodedPassword,
                artistName = artistName,
                slug = slug,
                registeredAt = Instant.now(),
            )
        )
        logger.info("New tattoo artist registration: {}", email)
        return id
    }

    override fun findUnactivatedByEmail(email: String): TattooArtist? =
        repository.findByEmailAndActivatedAtIsNull(email)

    override fun findByEmail(email: String): TattooArtist? = repository.findByEmail(email)

    override fun findBySlug(slug: String): TattooArtist =
        repository.findBySlug(slug).orElseThrow { TattooArtistNotFoundException() }

    override fun existsBySlug(slug: String): Boolean = repository.existsBySlug(slug)

    override fun existsBySlugAndIdNot(slug: String, artistId: UUID): Boolean =
        repository.existsBySlugAndIdNot(slug, artistId)

    override fun findById(artistId: UUID): TattooArtist =
        repository.findById(artistId).orElseThrow { TattooArtistNotFoundException() }

    @Transactional
    override fun activate(artistId: UUID) {
        val artist = repository.findById(artistId)
            .orElseThrow { IllegalStateException("Artist $artistId not found for activation") }
        artist.activatedAt = Instant.now()
        repository.save(artist)
        logger.info("Activated tattoo artist: {}", artist.email)
    }

    @Transactional
    override fun updateProfile(artistId: UUID, artistName: String?, slug: String?): ProfileResponseDto {
        val artist = findById(artistId)
        artistName?.let { artist.artistName = it }
        slug?.let { artist.slug = it }
        val updatedArtist = repository.save(artist)
        val profilePhotoUrl = updatedArtist.profilePhotoKey?.let { storageService.getSignedUrl(it) }
        return ProfileResponseDto(updatedArtist, profilePhotoUrl)
    }

    override fun getProfile(artistId: UUID): ProfileResponseDto {
        val artist = findById(artistId)
        val profilePhotoUrl = artist.profilePhotoKey?.let { storageService.getSignedUrl(it) }
        return ProfileResponseDto(artist, profilePhotoUrl)
    }

    @Transactional
    override fun updatePhoto(artistId: UUID, photo: MultipartFile): String {
        val artist = findById(artistId)
        val photoKey = "artists/$artistId/profile-photo"
        artist.profilePhotoKey = photoKey
        repository.save(artist)
        return storageService.upload(photoKey, photo.bytes, photo.contentType!!)
    }
}
