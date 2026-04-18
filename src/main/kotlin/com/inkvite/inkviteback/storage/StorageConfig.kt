package com.inkvite.inkviteback.storage

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("app.storage")
data class StorageConfig(
    val endpoint: String = "",
    val bucket: String = "",
    val accessKey: String = "",
    val secretKey: String = "",
)
