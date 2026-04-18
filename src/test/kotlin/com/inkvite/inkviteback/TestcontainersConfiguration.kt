package com.inkvite.inkviteback

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.springframework.test.context.DynamicPropertyRegistry
import org.testcontainers.containers.MinIOContainer
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.CreateBucketRequest
import java.net.URI

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

    companion object {
        val minioContainer: MinIOContainer = MinIOContainer("minio/minio:latest")
            .also { container ->
                container.start()
                S3Client.builder()
                    .endpointOverride(URI.create(container.s3URL))
                    .region(Region.EU_WEST_3)
                    .credentialsProvider(
                        StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(container.userName, container.password)
                        )
                    )
                    .forcePathStyle(true)
                    .build()
                    .createBucket(CreateBucketRequest.builder().bucket("inkvite").build())
            }

        fun registerStorageProperties(registry: DynamicPropertyRegistry) {
            registry.add("app.storage.endpoint") { minioContainer.s3URL }
            registry.add("app.storage.access-key") { minioContainer.userName }
            registry.add("app.storage.secret-key") { minioContainer.password }
            registry.add("app.storage.bucket") { "inkvite" }
        }
    }

    @Bean
    @ServiceConnection
    fun postgresContainer(): PostgreSQLContainer =
        PostgreSQLContainer(DockerImageName.parse("postgres:17"))
}
