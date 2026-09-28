package com.inkvite.inkviteback.auth

import java.time.Duration

enum class Role(val claim: String, val authority: String, val refreshTokenDuration: Duration) {
    ARTIST("artist", "ROLE_ARTIST", Duration.ofDays(30)),
    CLIENT("client", "ROLE_CLIENT", Duration.ofDays(2));

    companion object {
        fun fromClaim(claim: String?): Role? = entries.firstOrNull { it.claim == claim }
    }
}
