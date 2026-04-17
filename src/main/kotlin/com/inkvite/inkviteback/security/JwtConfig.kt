package com.inkvite.inkviteback.security

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.jwt")
data class JwtConfig(
    val secret: String,
    val accessTokenExpiry: Long = 900,
)
