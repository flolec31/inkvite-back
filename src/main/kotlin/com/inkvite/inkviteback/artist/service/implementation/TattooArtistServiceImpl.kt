package com.inkvite.inkviteback.artist.service.implementation

import com.inkvite.inkviteback.artist.entity.TattooArtist
import com.inkvite.inkviteback.artist.exception.TattooArtistAlreadyExistsException
import com.inkvite.inkviteback.artist.repository.TattooArtistRepository
import com.inkvite.inkviteback.artist.service.TattooArtistService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

@Service
@Transactional
class TattooArtistServiceImpl(
    private val repository: TattooArtistRepository
) : TattooArtistService {

    override fun register(id: UUID, email: String, encodedPassword: String) {
        if (repository.existsByEmail(email)) throw TattooArtistAlreadyExistsException()
        val tattooArtist = TattooArtist(
            id = id,
            email = email,
            password = encodedPassword,
            registeredAt = Instant.now()
        )
        repository.save(tattooArtist)
    }

    override fun activate(artistId: UUID) {
        val artist = repository.findById(artistId)
            .orElseThrow { IllegalStateException("Artist $artistId not found for activation") }
        artist.activatedAt = Instant.now()
        repository.save(artist)
    }
}