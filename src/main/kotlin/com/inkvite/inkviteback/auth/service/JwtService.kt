package com.inkvite.inkviteback.auth.service

import java.util.UUID

fun interface JwtService {
    fun generateAccessToken(artistId: UUID): String
}
