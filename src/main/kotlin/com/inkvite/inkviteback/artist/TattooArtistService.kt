package com.inkvite.inkviteback.artist

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class TattooArtistService(private val repository: TattooArtistRepository) {

    fun register(id: UUID, email: String, encodedPassword: String) {
        if (repository.existsByEmail(email)) throw TattooArtistAlreadyExistsException()
        repository.save(TattooArtist(id = id, email = email, passwordHash = encodedPassword))
    }

    fun activate(artistId: UUID) {
        val artist = repository.findById(artistId)
            .orElseThrow { IllegalStateException("Artist $artistId not found for activation") }
        artist.active = true
        repository.save(artist)
    }
}