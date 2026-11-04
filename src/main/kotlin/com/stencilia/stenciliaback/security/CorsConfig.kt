package com.stencilia.stenciliaback.security

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.cors")
data class CorsConfig(
    val allowedOrigins: List<String> = emptyList(),
)
