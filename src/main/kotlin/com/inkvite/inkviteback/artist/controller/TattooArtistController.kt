package com.inkvite.inkviteback.artist.controller

import com.inkvite.inkviteback.artist.dto.PhotoUploadResponseDto
import com.inkvite.inkviteback.artist.dto.ProfileResponseDto
import com.inkvite.inkviteback.artist.dto.SlugAvailabilityResponseDto
import com.inkvite.inkviteback.artist.dto.UpdateProfileRequestDto
import com.inkvite.inkviteback.artist.exception.InvalidPhotoContentTypeException
import com.inkvite.inkviteback.artist.exception.SlugAlreadyTakenException
import com.inkvite.inkviteback.artist.service.TattooArtistService
import jakarta.validation.Valid
import jakarta.validation.constraints.Pattern
import org.springframework.http.MediaType
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.*

@RestController
@RequestMapping("/artists")
@Validated
class TattooArtistController(
    private val tattooArtistService: TattooArtistService
) {

    @GetMapping("/slug-available")
    fun isSlugAvailable(
        @RequestParam @Pattern(regexp = "^[a-z0-9][a-z0-9-]{1,28}[a-z0-9]$") slug: String
    ): SlugAvailabilityResponseDto =
        SlugAvailabilityResponseDto(available = !tattooArtistService.existsBySlug(slug))

    @PatchMapping("/me")
    fun updateProfile(
        @Valid @RequestBody request: UpdateProfileRequestDto,
        authentication: JwtAuthenticationToken
    ): ProfileResponseDto {
        val artistId = UUID.fromString(authentication.token.subject)
        if (request.slug != null && tattooArtistService.existsBySlugAndIdNot(request.slug, artistId)) {
            throw SlugAlreadyTakenException()
        }
        return tattooArtistService.updateProfile(artistId, request.artistName, request.slug)
    }

    @GetMapping("/me")
    fun getProfile(
        authentication: JwtAuthenticationToken
    ): ProfileResponseDto {
        val artistId = UUID.fromString(authentication.token.subject)
        return tattooArtistService.getProfile(artistId)
    }

    @PostMapping("/me/photo", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadPhoto(
        @RequestParam("photo") photo: MultipartFile,
        authentication: JwtAuthenticationToken
    ): PhotoUploadResponseDto {
        val allowedTypes = setOf("image/jpeg", "image/png", "image/webp")
        if (photo.contentType !in allowedTypes) throw InvalidPhotoContentTypeException()

        val artistId = UUID.fromString(authentication.token.subject)
        val photoUrl = tattooArtistService.updatePhoto(artistId, photo)
        return PhotoUploadResponseDto(photoUrl = photoUrl)
    }
}