package com.inkvite.inkviteback.auth.service

import com.inkvite.inkviteback.auth.Role
import java.util.UUID

fun interface JwtService {
    fun generateAccessToken(subjectId: UUID, role: Role): String
}
