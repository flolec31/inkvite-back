package com.inkvite.inkviteback.appointment

import com.inkvite.inkviteback.appointment.entity.Reference
import com.inkvite.inkviteback.auth.Role
import com.inkvite.inkviteback.auth.service.JwtService
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.*

class AppointmentClientIntegrationTest : AbstractAppointmentIntegrationTest() {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var jwtService: JwtService

    // --- GET /client/appointment/{appointmentId} ---

    @Test
    fun `get client appointment returns 401 when not authenticated`() {
        mockMvc.perform(get("/client/appointment/${UUID.randomUUID()}"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `get client appointment returns 403 when called with an artist token`() {
        val artist = createActivatedArtist()
        val client = createClient()
        val appointment = createAppointment(artist, client)
        val artistToken = jwtService.generateAccessToken(artist.id, Role.ARTIST)

        mockMvc.perform(
            get("/client/appointment/${appointment.id}").header("Authorization", "Bearer $artistToken")
        ).andExpect(status().isForbidden)
    }

    @Test
    fun `get client appointment returns 404 when appointment does not exist`() {
        val client = createClient()
        val token = jwtService.generateAccessToken(client.id, Role.CLIENT)

        mockMvc.perform(
            get("/client/appointment/${UUID.randomUUID()}").header("Authorization", "Bearer $token")
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.error").value("Appointment not found"))
    }

    @Test
    fun `get client appointment returns 404 when appointment is not verified`() {
        val artist = createActivatedArtist()
        val client = createClient()
        val appointment = createAppointment(artist, client, verifiedAt = null)
        val token = jwtService.generateAccessToken(client.id, Role.CLIENT)

        mockMvc.perform(
            get("/client/appointment/${appointment.id}").header("Authorization", "Bearer $token")
        ).andExpect(status().isNotFound)
    }

    @Test
    fun `get client appointment returns 404 when appointment belongs to another client`() {
        val artist = createActivatedArtist()
        val owner = createClient(email = "owner@test.com")
        val other = createClient(email = "other@test.com")
        val appointment = createAppointment(artist, owner)
        val otherToken = jwtService.generateAccessToken(other.id, Role.CLIENT)

        mockMvc.perform(
            get("/client/appointment/${appointment.id}").header("Authorization", "Bearer $otherToken")
        ).andExpect(status().isNotFound)
    }

    @Test
    fun `get client appointment returns tattoo fields with artist name and no artist-inbox fields`() {
        val artist = createActivatedArtist()
        val client = createClient()
        val appointment = createAppointment(artist, client)
        val token = jwtService.generateAccessToken(client.id, Role.CLIENT)

        mockMvc.perform(
            get("/client/appointment/${appointment.id}").header("Authorization", "Bearer $token")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(appointment.id.toString()))
            .andExpect(jsonPath("$.tattooDescription").value("A beautiful dragon tattoo"))
            .andExpect(jsonPath("$.tattooPlacement").value("forearm"))
            .andExpect(jsonPath("$.tattooSize").value("10x10cm"))
            .andExpect(jsonPath("$.firstTattoo").value(false))
            .andExpect(jsonPath("$.coverUp").value(false))
            .andExpect(jsonPath("$.color").value(false))
            .andExpect(jsonPath("$.style").value("REALISM"))
            .andExpect(jsonPath("$.receivedAt").isString)
            .andExpect(jsonPath("$.references").isArray)
            .andExpect(jsonPath("$.artistName").value("Test Artist"))
            .andExpect(jsonPath("$.clientName").doesNotExist())
            .andExpect(jsonPath("$.new").doesNotExist())
            .andExpect(jsonPath("$.archived").doesNotExist())
    }

    @Test
    fun `get client appointment returns signed urls for references`() {
        val artist = createActivatedArtist()
        val client = createClient()
        val appointment = createAppointment(artist, client)
        referenceRepository.save(
            Reference(appointment = appointment, key = "references/${artist.id}/ref1.jpg", comment = "Like this")
        )
        val token = jwtService.generateAccessToken(client.id, Role.CLIENT)

        mockMvc.perform(
            get("/client/appointment/${appointment.id}").header("Authorization", "Bearer $token")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.references.length()").value(1))
            .andExpect(jsonPath("$.references[0].url").value(containsString("X-Amz-Signature")))
            .andExpect(jsonPath("$.references[0].comment").value("Like this"))
    }

    @Test
    fun `get client appointment does not flip the new flag`() {
        val artist = createActivatedArtist()
        val client = createClient()
        val appointment = createAppointment(artist, client)
        assertThat(appointment.new).isTrue()
        val token = jwtService.generateAccessToken(client.id, Role.CLIENT)

        mockMvc.perform(
            get("/client/appointment/${appointment.id}").header("Authorization", "Bearer $token")
        ).andExpect(status().isOk)

        assertThat(appointmentRepository.findById(appointment.id).get().new).isTrue()
    }
}
