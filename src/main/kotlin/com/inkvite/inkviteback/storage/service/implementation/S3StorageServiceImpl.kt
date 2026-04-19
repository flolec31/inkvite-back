package com.inkvite.inkviteback.storage.service.implementation

import com.inkvite.inkviteback.storage.StorageConfig
import com.inkvite.inkviteback.storage.service.StorageService
import org.springframework.stereotype.Service
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.net.URI

@Service
class S3StorageServiceImpl(
    private val config: StorageConfig
) : StorageService {

    private val client: S3Client by lazy {
        S3Client.builder()
            .endpointOverride(URI.create(config.endpoint))
            .region(Region.EU_WEST_3)
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(config.accessKey, config.secretKey)
                )
            )
            .forcePathStyle(true)
            .build()
    }

    override fun upload(key: String, bytes: ByteArray, contentType: String): String {
        client.putObject(
            PutObjectRequest.builder()
                .bucket(config.bucket)
                .key(key)
                .contentType(contentType)
                .build(),
            RequestBody.fromBytes(bytes)
        )
        return "${config.endpoint}/${config.bucket}/$key"
    }

    override fun baseUrl(): String = "${config.endpoint}/${config.bucket}"
}