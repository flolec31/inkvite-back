package com.inkvite.inkviteback.appointment

import com.inkvite.inkviteback.appointment.exception.InvalidReferenceContentTypeException
import com.inkvite.inkviteback.appointment.exception.ReferenceTooLargeException
import com.inkvite.inkviteback.appointment.exception.ReferenceUploadFailedException
import com.inkvite.inkviteback.appointment.service.implementation.AppointmentServiceImpl
import com.inkvite.inkviteback.artist.entity.TattooArtist
import com.inkvite.inkviteback.artist.service.TattooArtistService
import com.inkvite.inkviteback.storage.service.StorageService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.springframework.mock.web.MockMultipartFile
import java.time.Instant
import java.util.*

@ExtendWith(MockitoExtension::class)
class AppointmentServiceImplTest {

    @Mock
    private lateinit var tattooArtistService: TattooArtistService

    @Mock
    private lateinit var storageService: StorageService

    private lateinit var service: AppointmentServiceImpl

    @BeforeEach
    fun setUp() {
        service = AppointmentServiceImpl(
            mock(), tattooArtistService, mock(), storageService, mock(), mock()
        )
    }

    private val artistId = UUID.randomUUID()
    private val artist = TattooArtist(
        id = artistId,
        email = "artist@test.com",
        password = "hashed",
        artistName = "Test Artist",
        slug = "test-artist",
        registeredAt = Instant.now(),
        activatedAt = Instant.now(),
    )

    @Test
    fun `uploadReference returns model with key and url on valid jpeg`() {
        val photo = MockMultipartFile("photo", "ref.jpg", "image/jpeg", ByteArray(100))
        `when`(tattooArtistService.findBySlug("test-artist")).thenReturn(artist)
        `when`(storageService.upload(any(), any(), eq("image/jpeg")))
            .thenReturn("https://r2.example.com/signed-url")

        val result = service.uploadReference("test-artist", photo)

        assertThat(result.key).matches("references/$artistId/[0-9a-f\\-]{36}")
        assertThat(result.url).isEqualTo("https://r2.example.com/signed-url")
    }

    @Test
    fun `uploadReference throws InvalidReferenceContentTypeException for unsupported type`() {
        val photo = MockMultipartFile("photo", "file.pdf", "application/pdf", ByteArray(100))

        assertThrows<InvalidReferenceContentTypeException> {
            service.uploadReference("test-artist", photo)
        }
    }

    @Test
    fun `uploadReference throws InvalidReferenceContentTypeException when content type is null`() {
        val photo = MockMultipartFile("photo", "file", null, ByteArray(100))

        assertThrows<InvalidReferenceContentTypeException> {
            service.uploadReference("test-artist", photo)
        }
    }

    @Test
    fun `uploadReference throws ReferenceTooLargeException when file exceeds 5mb`() {
        val photo = MockMultipartFile("photo", "big.jpg", "image/jpeg", ByteArray(5 * 1024 * 1024 + 1))

        assertThrows<ReferenceTooLargeException> {
            service.uploadReference("test-artist", photo)
        }
    }

    @Test
    fun `uploadReference wraps storage exception in ReferenceUploadFailedException`() {
        val photo = MockMultipartFile("photo", "ref.jpg", "image/jpeg", ByteArray(100))
        val cause = RuntimeException("S3 unavailable")
        `when`(tattooArtistService.findBySlug("test-artist")).thenReturn(artist)
        `when`(storageService.upload(any(), any(), any())).thenThrow(cause)

        val ex = assertThrows<ReferenceUploadFailedException> {
            service.uploadReference("test-artist", photo)
        }
        assertThat(ex.cause).isSameAs(cause)
    }
}
