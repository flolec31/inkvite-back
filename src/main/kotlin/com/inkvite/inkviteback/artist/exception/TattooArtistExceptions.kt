package com.inkvite.inkviteback.artist.exception

class TattooArtistAlreadyExistsException : RuntimeException("A tattoo artist with this email already exists")

class SlugAlreadyTakenException : RuntimeException("This slug is already taken")

class TattooArtistNotFoundException : RuntimeException("Tattoo artist not found")