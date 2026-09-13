package com.inkvite.inkviteback.auth

import com.inkvite.inkviteback.AbstractIntegrationTest
import com.inkvite.inkviteback.artist.entity.TattooArtist
import com.inkvite.inkviteback.artist.repository.TattooArtistRepository
import com.inkvite.inkviteback.auth.service.JwtService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant
import java.util.*

class RbacIntegrationTest : AbstractIntegrationTest() {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var jwtService: JwtService
    @Autowired lateinit var artistRepository: TattooArtistRepository

    @BeforeEach @AfterEach
    fun cleanup() = artistRepository.deleteAll().let {}

    @Test
    fun `artist token can reach an artist route`() {
        val artist = artistRepository.save(
            TattooArtist(
                id = UUID.randomUUID(), email = "a@test.com", password = "hashed",
                artistName = "A", slug = "a", city = "c", countryCode = "FR",
                registeredAt = Instant.now(), activatedAt = Instant.now(),
            )
        )
        val token = jwtService.generateAccessToken(artist.id, Role.ARTIST)
        mockMvc.perform(get("/artists/me").header("Authorization", "Bearer $token"))
            .andExpect(status().isOk)
    }
}
