package com.inkvite.inkviteback

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.springframework.test.context.DynamicPropertyRegistry
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
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
        private const val BUCKET = "inkvite"
        private const val S3_PORT = 9090
        private const val ACCESS_KEY = "test"
        private const val SECRET_KEY = "test"

        val s3Container: GenericContainer<*> =
            GenericContainer(DockerImageName.parse("adobe/s3mock:5.2.3"))
                .withExposedPorts(S3_PORT)
                .waitingFor(Wait.forHttp("/").forPort(S3_PORT).forStatusCode(200))
                .also { container ->
                    container.start()
                    S3Client.builder()
                        .endpointOverride(URI.create(s3Url(container)))
                        .region(Region.EU_WEST_3)
                        .credentialsProvider(
                            StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(ACCESS_KEY, SECRET_KEY)
                            )
                        )
                        .forcePathStyle(true)
                        .build()
                        .createBucket(CreateBucketRequest.builder().bucket(BUCKET).build())
                }

        private fun s3Url(container: GenericContainer<*>): String =
            "http://${container.host}:${container.getMappedPort(S3_PORT)}"

        val s3Endpoint: String get() = s3Url(s3Container)
        val s3AccessKey: String get() = ACCESS_KEY
        val s3SecretKey: String get() = SECRET_KEY

        fun registerStorageProperties(registry: DynamicPropertyRegistry) {
            registry.add("app.storage.endpoint") { s3Endpoint }
            registry.add("app.storage.access-key") { s3AccessKey }
            registry.add("app.storage.secret-key") { s3SecretKey }
            registry.add("app.storage.bucket") { BUCKET }
        }
    }

    @Bean
    @ServiceConnection
    fun postgresContainer(): PostgreSQLContainer =
        PostgreSQLContainer(DockerImageName.parse("postgres:17"))
}
