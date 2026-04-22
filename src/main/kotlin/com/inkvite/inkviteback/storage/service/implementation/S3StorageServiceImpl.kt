package com.inkvite.inkviteback.storage.service.implementation

import com.inkvite.inkviteback.storage.StorageConfig
import com.inkvite.inkviteback.storage.service.StorageService
import org.springframework.stereotype.Service
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.S3Configuration
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import java.net.URI
import java.time.Duration

@Service
class S3StorageServiceImpl(
    private val config: StorageConfig
) : StorageService {

    private val credentials by lazy {
        StaticCredentialsProvider.create(
            AwsBasicCredentials.create(config.accessKey, config.secretKey)
        )
    }

    private val client: S3Client by lazy {
        S3Client.builder()
            .endpointOverride(URI.create(config.endpoint))
            .region(Region.EU_WEST_3)
            .credentialsProvider(credentials)
            .forcePathStyle(true)
            .build()
    }

    private val presigner: S3Presigner by lazy {
        S3Presigner.builder()
            .endpointOverride(URI.create(config.endpoint))
            .region(Region.EU_WEST_3)
            .credentialsProvider(credentials)
            .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
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
        return getSignedUrl(key)
    }

    override fun getSignedUrl(key: String): String =
        presigner.presignGetObject(
            GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofHours(1))
                .getObjectRequest(
                    GetObjectRequest.builder()
                        .bucket(config.bucket)
                        .key(key)
                        .build()
                )
                .build()
        ).url().toString()
}
