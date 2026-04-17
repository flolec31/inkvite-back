package com.inkvite.inkviteback.artist.service.implementation

import com.inkvite.inkviteback.artist.entity.TattooArtist
import com.inkvite.inkviteback.artist.exception.TattooArtistAlreadyExistsException
import com.inkvite.inkviteback.artist.repository.TattooArtistRepository
import com.inkvite.inkviteback.artist.service.TattooArtistService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

@Service
@Transactional(readOnly = true)
class TattooArtistServiceImpl(
    private val repository: TattooArtistRepository
) : TattooArtistService {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    override fun register(email: String, encodedPassword: String): UUID {
        if (repository.existsByEmail(email)) throw TattooArtistAlreadyExistsException()
        val id = UUID.randomUUID()
        repository.save(TattooArtist(id = id, email = email, password = encodedPassword, registeredAt = Instant.now()))
        logger.info("New tattoo artist registration: {}", email)
        return id
    }

    override fun findUnactivatedByEmail(email: String): UUID? =
        repository.findByEmailAndActivatedAtIsNull(email)?.id

    override fun findByEmail(email: String): TattooArtist? = repository.findByEmail(email)

    @Transactional
    override fun activate(artistId: UUID) {
        val artist = repository.findById(artistId)
            .orElseThrow { IllegalStateException("Artist $artistId not found for activation") }
        artist.activatedAt = Instant.now()
        repository.save(artist)
        logger.info("Activated tattoo artist: {}", artist.email)
    }
}
