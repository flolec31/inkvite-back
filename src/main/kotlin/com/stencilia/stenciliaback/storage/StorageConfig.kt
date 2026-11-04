package com.stencilia.stenciliaback.storage

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("app.storage")
data class StorageConfig(
    val endpoint: String = "",
    val bucket: String = "",
    val accessKey: String = "",
    val secretKey: String = "",
    val region: String = "",
)
