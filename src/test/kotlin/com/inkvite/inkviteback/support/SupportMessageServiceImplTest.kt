package com.inkvite.inkviteback.support

import com.inkvite.inkviteback.artist.entity.TattooArtist
import com.inkvite.inkviteback.artist.service.TattooArtistService
import com.inkvite.inkviteback.storage.dto.ImageUploadResponseDto
import com.inkvite.inkviteback.storage.service.ImageUploadService
import com.inkvite.inkviteback.support.dto.SupportMessageRequestDto
import com.inkvite.inkviteback.support.entity.SupportMessageType
import com.inkvite.inkviteback.support.event.SupportMessageConfirmationEmailRequested
import com.inkvite.inkviteback.support.event.SupportMessageReceivedEmailRequested
import com.inkvite.inkviteback.support.repository.SupportMessageRepository
import com.inkvite.inkviteback.support.service.implementation.SupportMessageServiceImpl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.context.ApplicationEventPublisher
import org.springframework.mock.web.MockMultipartFile
import java.time.Instant
import java.util.*

@ExtendWith(MockitoExtension::class)
class SupportMessageServiceImplTest {

    @Mock private lateinit var tattooArtistService: TattooArtistService
    @Mock private lateinit var imageUploadService: ImageUploadService
    @Mock private lateinit var supportMessageRepository: SupportMessageRepository
    @Mock private lateinit var eventPublisher: ApplicationEventPublisher

    @Captor
    private lateinit var savedCaptor: ArgumentCaptor<com.inkvite.inkviteback.support.entity.SupportMessage>

    @Captor
    private lateinit var eventCaptor: ArgumentCaptor<Any>

    private lateinit var service: SupportMessageServiceImpl

    @BeforeEach
    fun setUp() {
        service = SupportMessageServiceImpl(tattooArtistService, imageUploadService, supportMessageRepository, eventPublisher)
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
    fun `uploadScreenshot delegates to ImageUploadService`() {
        val photo = MockMultipartFile("photo", "shot.png", "image/png", ByteArray(100))
        val expected = ImageUploadResponseDto(key = "contact-screenshots/$artistId/uuid", url = "https://r2.example.com/signed-url")
        `when`(imageUploadService.uploadSupportScreenshot(artistId, photo)).thenReturn(expected)

        val result = service.uploadScreenshot(artistId, photo)

        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `submit saves a message without a screenshot and publishes notification and confirmation events`() {
        `when`(tattooArtistService.findById(artistId)).thenReturn(artist)
        `when`(supportMessageRepository.save(savedCaptor.capture())).thenAnswer { savedCaptor.value }
        val request = SupportMessageRequestDto(type = SupportMessageType.HELP, message = "How do I change my slug?")

        service.submit(artistId, request)

        val saved = savedCaptor.value
        assertThat(saved.artist.id).isEqualTo(artistId)
        assertThat(saved.type).isEqualTo(SupportMessageType.HELP)
        assertThat(saved.message).isEqualTo("How do I change my slug?")
        assertThat(saved.screenshotKey).isNull()
        assertThat(saved.createdAt).isNotNull()

        verify(eventPublisher, org.mockito.Mockito.times(2)).publishEvent(eventCaptor.capture())
        val receivedEvent = eventCaptor.allValues.filterIsInstance<SupportMessageReceivedEmailRequested>().single()
        assertThat(receivedEvent.supportMessage).isEqualTo(saved)
        val confirmationEvent = eventCaptor.allValues.filterIsInstance<SupportMessageConfirmationEmailRequested>().single()
        assertThat(confirmationEvent.to).isEqualTo("artist@test.com")
        assertThat(confirmationEvent.artistName).isEqualTo("Test Artist")
    }

    @Test
    fun `submit saves a message with a screenshot's key`() {
        `when`(tattooArtistService.findById(artistId)).thenReturn(artist)
        `when`(supportMessageRepository.save(savedCaptor.capture())).thenAnswer { savedCaptor.value }
        val request = SupportMessageRequestDto(
            type = SupportMessageType.BUG,
            message = "The upload button is broken",
            screenshot = "contact-screenshots/$artistId/abc",
        )

        service.submit(artistId, request)

        val saved = savedCaptor.value
        assertThat(saved.screenshotKey).isEqualTo("contact-screenshots/$artistId/abc")
    }
}
