package com.inkvite.inkviteback.storage.service.implementation

import com.inkvite.inkviteback.storage.exception.ImageTooLargeException
import com.inkvite.inkviteback.storage.exception.ImageUploadFailedException
import com.inkvite.inkviteback.storage.exception.InvalidImageContentTypeException
import com.inkvite.inkviteback.storage.ImageValidator
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
import org.springframework.mock.web.MockMultipartFile
import java.util.*

@ExtendWith(MockitoExtension::class)
class ImageUploadServiceImplTest {

    @Mock
    private lateinit var storageService: StorageService

    private lateinit var service: ImageUploadServiceImpl

    @BeforeEach
    fun setUp() {
        service = ImageUploadServiceImpl(storageService, ImageValidator())
    }

    private val artistId = UUID.randomUUID()

    @Test
    fun `uploadReference returns key under references prefix and url on valid jpeg`() {
        val photo = MockMultipartFile("photo", "ref.jpg", "image/jpeg", ByteArray(100))
        `when`(storageService.upload(any(), any(), eq("image/jpeg")))
            .thenReturn("https://r2.example.com/signed-url")

        val result = service.uploadReference(artistId, photo)

        assertThat(result.key).matches("references/$artistId/[0-9a-f\\-]{36}")
        assertThat(result.url).isEqualTo("https://r2.example.com/signed-url")
    }

    @Test
    fun `uploadSupportScreenshot returns key under contact-screenshots prefix`() {
        val photo = MockMultipartFile("photo", "shot.png", "image/png", ByteArray(100))
        `when`(storageService.upload(any(), any(), eq("image/png")))
            .thenReturn("https://r2.example.com/signed-url")

        val result = service.uploadSupportScreenshot(artistId, photo)

        assertThat(result.key).matches("contact-screenshots/$artistId/[0-9a-f\\-]{36}")
        assertThat(result.url).isEqualTo("https://r2.example.com/signed-url")
    }

    @Test
    fun `uploadReference throws InvalidImageContentTypeException for unsupported type`() {
        val photo = MockMultipartFile("photo", "file.pdf", "application/pdf", ByteArray(100))

        assertThrows<InvalidImageContentTypeException> {
            service.uploadReference(artistId, photo)
        }
    }

    @Test
    fun `uploadReference throws InvalidImageContentTypeException when content type is null`() {
        val photo = MockMultipartFile("photo", "file", null, ByteArray(100))

        assertThrows<InvalidImageContentTypeException> {
            service.uploadReference(artistId, photo)
        }
    }

    @Test
    fun `uploadReference throws ImageTooLargeException when file exceeds 5mb`() {
        val photo = MockMultipartFile("photo", "big.jpg", "image/jpeg", ByteArray(5 * 1024 * 1024 + 1))

        assertThrows<ImageTooLargeException> {
            service.uploadReference(artistId, photo)
        }
    }

    @Test
    fun `uploadSupportScreenshot throws ImageTooLargeException when file exceeds 5mb`() {
        val photo = MockMultipartFile("photo", "big.png", "image/png", ByteArray(5 * 1024 * 1024 + 1))

        assertThrows<ImageTooLargeException> {
            service.uploadSupportScreenshot(artistId, photo)
        }
    }

    @Test
    fun `uploadReference wraps storage exception in ImageUploadFailedException`() {
        val photo = MockMultipartFile("photo", "ref.jpg", "image/jpeg", ByteArray(100))
        val cause = RuntimeException("S3 unavailable")
        `when`(storageService.upload(any(), any(), any())).thenThrow(cause)

        val ex = assertThrows<ImageUploadFailedException> {
            service.uploadReference(artistId, photo)
        }
        assertThat(ex.cause).isSameAs(cause)
    }
}
