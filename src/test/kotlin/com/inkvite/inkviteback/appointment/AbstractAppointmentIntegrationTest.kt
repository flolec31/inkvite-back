package com.inkvite.inkviteback.appointment

import com.inkvite.inkviteback.AbstractIntegrationTest
import com.inkvite.inkviteback.appointment.entity.Appointment
import com.inkvite.inkviteback.appointment.entity.TattooStyle
import com.inkvite.inkviteback.appointment.repository.AppointmentRepository
import com.inkvite.inkviteback.appointment.repository.ReferenceRepository
import com.inkvite.inkviteback.artist.entity.TattooArtist
import com.inkvite.inkviteback.artist.repository.TattooArtistRepository
import com.inkvite.inkviteback.client.entity.TattooClient
import com.inkvite.inkviteback.client.repository.TattooClientRepository
import com.inkvite.inkviteback.discussion.repository.MessageRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import java.time.Instant
import java.util.*

abstract class AbstractAppointmentIntegrationTest : AbstractIntegrationTest() {

    @Autowired lateinit var artistRepository: TattooArtistRepository
    @Autowired lateinit var appointmentRepository: AppointmentRepository
    @Autowired lateinit var tattooClientRepository: TattooClientRepository
    @Autowired lateinit var referenceRepository: ReferenceRepository
    @Autowired lateinit var messageRepository: MessageRepository

    @BeforeEach
    @AfterEach
    fun cleanupAppointments() {
        messageRepository.deleteAll()
        referenceRepository.deleteAll()
        appointmentRepository.deleteAll()
        tattooClientRepository.deleteAll()
        artistRepository.deleteAll()
    }

    protected fun createActivatedArtist(slug: String = "test-artist"): TattooArtist =
        artistRepository.save(
            TattooArtist(
                id = UUID.randomUUID(),
                email = "$slug@test.com",
                password = "hashed",
                artistName = "Test Artist",
                slug = slug,
                city = "Test City",
                countryCode = "FR",
                registeredAt = Instant.now(),
                activatedAt = Instant.now(),
            )
        )

    protected fun createClient(email: String = "client@test.com"): TattooClient =
        tattooClientRepository.save(TattooClient(email = email, firstName = "Jane", lastName = "Doe"))

    protected fun createAppointment(
        artist: TattooArtist,
        client: TattooClient,
        verifiedAt: Instant? = Instant.now(),
        archived: Boolean = false,
        description: String = "A beautiful dragon tattoo",
        submittedAt: Instant = Instant.now(),
    ): Appointment =
        appointmentRepository.save(
            Appointment(
                artist = artist,
                client = client,
                tattooDescription = description,
                tattooPlacement = "forearm",
                tattooSize = "10x10cm",
                firstTattoo = false,
                coverUp = false,
                color = false,
                style = TattooStyle.REALISM,
                submittedAt = submittedAt,
                verifiedAt = verifiedAt,
                archived = archived,
            )
        )
}
