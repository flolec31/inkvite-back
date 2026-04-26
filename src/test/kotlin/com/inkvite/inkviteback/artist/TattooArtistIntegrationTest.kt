package com.inkvite.inkviteback.artist

import com.inkvite.inkviteback.AbstractIntegrationTest
import com.inkvite.inkviteback.artist.entity.TattooArtist
import com.inkvite.inkviteback.artist.repository.TattooArtistRepository
import com.inkvite.inkviteback.auth.service.JwtService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant
import java.util.UUID

class TattooArtistIntegrationTest : AbstractIntegrationTest() {

    @Autowired
    lateinit var mockMvc: MockMvc
    @Autowired
    lateinit var artistRepository: TattooArtistRepository
    @Autowired
    lateinit var jwtService: JwtService

    @BeforeEach
    fun cleanup() {
        artistRepository.deleteAll()
    }

    private fun createActivatedArtist(
        email: String = "artist@test.com",
        slug: String = "test-artist",
    ): TattooArtist {
        val artist = TattooArtist(
            id = UUID.randomUUID(),
            email = email,
            password = "hashed",
            artistName = "Test Artist",
            slug = slug,
            registeredAt = Instant.now(),
            activatedAt = Instant.now(),
        )
        return artistRepository.save(artist)
    }

    // --- slug-available ---

    @Test
    fun `slug-available returns true for available slug`() {
        mockMvc.perform(get("/artists/slug-available").param("slug", "free-slug"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.available").value(true))
    }

    @Test
    fun `slug-available returns false for taken slug`() {
        createActivatedArtist(slug = "taken-slug")

        mockMvc.perform(get("/artists/slug-available").param("slug", "taken-slug"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.available").value(false))
    }

    @Test
    fun `slug-available returns 400 for invalid slug format`() {
        mockMvc.perform(get("/artists/slug-available").param("slug", "INVALID SLUG!"))
            .andExpect(status().isBadRequest)
    }

    // --- photo upload ---

    @Test
    fun `upload photo stores key and returns photo URL`() {
        val artist = createActivatedArtist()
        val token = jwtService.generateAccessToken(artist.id)
        val photoBytes = ByteArray(100) { it.toByte() }

        mockMvc.perform(
            MockMvcRequestBuilders.multipart("/artists/me/photo")
                .file(MockMultipartFile("photo", "photo.jpg", "image/jpeg", photoBytes))
                .header("Authorization", "Bearer $token")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.photoUrl").isString)

        val updated = artistRepository.findById(artist.id).get()
        assertThat(updated.profilePhotoKey).isEqualTo("artists/${artist.id}/profile-photo")
    }

    @Test
    fun `upload photo with invalid content type returns 400`() {
        val artist = createActivatedArtist()
        val token = jwtService.generateAccessToken(artist.id)

        mockMvc.perform(
            MockMvcRequestBuilders.multipart("/artists/me/photo")
                .file(MockMultipartFile("photo", "file.txt", "text/plain", ByteArray(10)))
                .header("Authorization", "Bearer $token")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Photo must be a JPEG, PNG, or WebP image"))
    }

    @Test
    fun `upload photo without authentication returns 401`() {
        mockMvc.perform(
            MockMvcRequestBuilders.multipart("/artists/me/photo")
                .file(MockMultipartFile("photo", "photo.jpg", "image/jpeg", ByteArray(10)))
        )
            .andExpect(status().isUnauthorized)
    }

    // --- PATCH /artists/me ---

    @Test
    fun `update profile with new artist name succeeds`() {
        val artist = createActivatedArtist()
        val token = jwtService.generateAccessToken(artist.id)

        mockMvc.perform(
            MockMvcRequestBuilders.patch("/artists/me")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content("""{"artistName":"New Name"}""")
                .header("Authorization", "Bearer $token")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.artistName").value("New Name"))
            .andExpect(jsonPath("$.slug").value("test-artist"))
    }

    @Test
    fun `update profile with new slug succeeds`() {
        val artist = createActivatedArtist()
        val token = jwtService.generateAccessToken(artist.id)

        mockMvc.perform(
            MockMvcRequestBuilders.patch("/artists/me")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content("""{"slug":"new-slug-42"}""")
                .header("Authorization", "Bearer $token")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.slug").value("new-slug-42"))
            .andExpect(jsonPath("$.artistName").value("Test Artist"))
    }

    @Test
    fun `update profile with both fields succeeds`() {
        val artist = createActivatedArtist()
        val token = jwtService.generateAccessToken(artist.id)

        mockMvc.perform(
            MockMvcRequestBuilders.patch("/artists/me")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content("""{"artistName":"New Name","slug":"new-slug-42"}""")
                .header("Authorization", "Bearer $token")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.artistName").value("New Name"))
            .andExpect(jsonPath("$.slug").value("new-slug-42"))
    }

    @Test
    fun `update profile with taken slug returns 409`() {
        createActivatedArtist(email = "other@test.com", slug = "taken-slug")
        val artist = createActivatedArtist(email = "artist@test.com", slug = "my-slug")
        val token = jwtService.generateAccessToken(artist.id)

        mockMvc.perform(
            MockMvcRequestBuilders.patch("/artists/me")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content("""{"slug":"taken-slug"}""")
                .header("Authorization", "Bearer $token")
        )
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.error").value("This slug is already taken"))
    }

    @Test
    fun `update profile with no fields returns 400`() {
        val artist = createActivatedArtist()
        val token = jwtService.generateAccessToken(artist.id)

        mockMvc.perform(
            MockMvcRequestBuilders.patch("/artists/me")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content("""{}""")
                .header("Authorization", "Bearer $token")
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `update profile without authentication returns 401`() {
        mockMvc.perform(
            MockMvcRequestBuilders.patch("/artists/me")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content("""{"artistName":"New Name"}""")
        )
            .andExpect(status().isUnauthorized)
    }

    // --- GET /artists/me ---

    @Test
    fun `get profile returns artist name, slug, and null photo url when no photo is set`() {
        val artist = createActivatedArtist()
        val token = jwtService.generateAccessToken(artist.id)

        mockMvc.perform(
            get("/artists/me")
                .header("Authorization", "Bearer $token")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.artistName").value("Test Artist"))
            .andExpect(jsonPath("$.slug").value("test-artist"))
            .andExpect(jsonPath("$.profilePhotoUrl").doesNotExist())
    }

    @Test
    fun `get profile without authentication returns 401`() {
        mockMvc.perform(get("/artists/me"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `get profile with jwt subject not matching any artist returns 404`() {
        val token = jwtService.generateAccessToken(UUID.randomUUID())

        mockMvc.perform(
            get("/artists/me")
                .header("Authorization", "Bearer $token")
        )
            .andExpect(status().isNotFound)
    }
}