package com.inkvite.inkviteback.auth.service.implementation

import com.inkvite.inkviteback.auth.service.JwtService
import com.inkvite.inkviteback.security.JwtConfig
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class JwtServiceImpl(
    private val jwtEncoder: JwtEncoder,
    private val jwtConfig: JwtConfig,
) : JwtService {

    override fun generateAccessToken(artistId: UUID): String {
        val now = Instant.now()
        val header = JwsHeader.with(MacAlgorithm.HS256).build()
        val claims = JwtClaimsSet.builder()
            .subject(artistId.toString())
            .issuedAt(now)
            .expiresAt(now.plusSeconds(jwtConfig.accessTokenExpiry))
            .build()
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).tokenValue
    }
}
