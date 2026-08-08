package com.inkvite.inkviteback.appointment

import com.inkvite.inkviteback.appointment.service.implementation.AppointmentSubmissionServiceImpl
import com.inkvite.inkviteback.artist.entity.TattooArtist
import com.inkvite.inkviteback.artist.service.TattooArtistService
import com.inkvite.inkviteback.storage.dto.ImageUploadResponseDto
import com.inkvite.inkviteback.storage.service.ImageUploadService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.springframework.mock.web.MockMultipartFile
import java.time.Instant
import java.util.*

@ExtendWith(MockitoExtension::class)
class AppointmentSubmissionServiceImplTest {

    @Mock
    private lateinit var tattooArtistService: TattooArtistService

    @Mock
    private lateinit var imageUploadService: ImageUploadService

    private lateinit var service: AppointmentSubmissionServiceImpl

    @BeforeEach
    fun setUp() {
        service = AppointmentSubmissionServiceImpl(
            mock(), tattooArtistService, mock(), imageUploadService, mock(), mock()
        )
    }

    private val artistId = UUID.randomUUID()
    private val artist = TattooArtist(
        id = artistId,
        email = "artist@test.com",
        password = "hashed",
        artistName = "Test Artist",
        slug = "test-artist",
        city = "Test City",
        countryCode = "FR",
        registeredAt = Instant.now(),
        activatedAt = Instant.now(),
    )

    @Test
    fun `uploadReference resolves artist by slug and delegates to ImageUploadService`() {
        val photo = MockMultipartFile("photo", "ref.jpg", "image/jpeg", ByteArray(100))
        val expected = ImageUploadResponseDto(key = "references/$artistId/uuid", url = "https://r2.example.com/signed-url")
        `when`(tattooArtistService.findBySlug("test-artist")).thenReturn(artist)
        `when`(imageUploadService.uploadReference(artistId, photo)).thenReturn(expected)

        val result = service.uploadReference("test-artist", photo)

        assertThat(result).isEqualTo(expected)
    }
}
