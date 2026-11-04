package com.stencilia.stenciliaback.auth.service

import com.stencilia.stenciliaback.auth.Role
import java.util.UUID

fun interface JwtService {
    fun generateAccessToken(subjectId: UUID, role: Role): String
}
