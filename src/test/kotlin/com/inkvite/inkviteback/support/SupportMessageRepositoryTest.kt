package com.inkvite.inkviteback.support

import com.inkvite.inkviteback.AbstractIntegrationTest
import com.inkvite.inkviteback.artist.entity.TattooArtist
import com.inkvite.inkviteback.artist.repository.TattooArtistRepository
import com.inkvite.inkviteback.support.entity.SupportMessage
import com.inkvite.inkviteback.support.entity.SupportMessageType
import com.inkvite.inkviteback.support.repository.SupportMessageRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant
import java.util.*

class SupportMessageRepositoryTest : AbstractIntegrationTest() {

    @Autowired lateinit var artistRepository: TattooArtistRepository
    @Autowired lateinit var supportMessageRepository: SupportMessageRepository

    @BeforeEach
    fun cleanup() {
        supportMessageRepository.deleteAll()
        artistRepository.deleteAll()
    }

    private fun createArtist(): TattooArtist =
        artistRepository.save(
            TattooArtist(
                id = UUID.randomUUID(),
                email = "artist@test.com",
                password = "hashed",
                artistName = "Test Artist",
                slug = "test-artist",
                city = "Test City",
                countryCode = "FR",
                registeredAt = Instant.now(),
                activatedAt = Instant.now(),
            )
        )

    @Test
    fun `saves and retrieves a support message without a screenshot`() {
        val artist = createArtist()
        val createdAt = Instant.now()

        val saved = supportMessageRepository.save(
            SupportMessage(
                artist = artist,
                type = SupportMessageType.BUG,
                message = "Something is broken",
                createdAt = createdAt,
            )
        )

        val found = supportMessageRepository.findById(saved.id).orElseThrow()
        assertThat(found.artist.id).isEqualTo(artist.id)
        assertThat(found.type).isEqualTo(SupportMessageType.BUG)
        assertThat(found.message).isEqualTo("Something is broken")
        assertThat(found.screenshotKey).isNull()
        assertThat(found.createdAt).isEqualTo(createdAt)
    }

    @Test
    fun `saves and retrieves a support message with a screenshot`() {
        val artist = createArtist()

        val saved = supportMessageRepository.save(
            SupportMessage(
                artist = artist,
                type = SupportMessageType.IDEA,
                message = "Add dark mode",
                screenshotKey = "contact-screenshots/${artist.id}/abc",
                createdAt = Instant.now(),
            )
        )

        val found = supportMessageRepository.findById(saved.id).orElseThrow()
        assertThat(found.screenshotKey).isEqualTo("contact-screenshots/${artist.id}/abc")
    }
}
