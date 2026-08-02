package com.inkvite.inkviteback.appointment

import com.inkvite.inkviteback.AbstractIntegrationTest
import com.inkvite.inkviteback.appointment.repository.AppointmentRepository
import com.inkvite.inkviteback.appointment.repository.ReferenceRepository
import com.inkvite.inkviteback.artist.entity.TattooArtist
import com.inkvite.inkviteback.artist.repository.TattooArtistRepository
import com.inkvite.inkviteback.client.repository.TattooClientRepository
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

    @BeforeEach
    @AfterEach
    fun cleanupAppointments() {
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
}
