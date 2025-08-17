package io.ark.springboot.client.s3

import io.ark.springboot.client.s3.S3Config
import org.junit.jupiter.api.Tag
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.TestConstructor
import org.testcontainers.containers.localstack.LocalStackContainer
import org.testcontainers.containers.localstack.LocalStackContainer.Service
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@ActiveProfiles("local")
@Tag("context")
@SpringBootTest(classes = [S3Config::class])
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Testcontainers
abstract class S3ClientContextTest {

    companion object {
        private val localstackImage = DockerImageName.parse("localstack/localstack:2.3.2")

        @Container
        @JvmStatic
        val localstack: LocalStackContainer = LocalStackContainer(localstackImage)
            .withServices(Service.S3)

        @JvmStatic
        @DynamicPropertySource
        fun registerS3Properties(registry: DynamicPropertyRegistry) {
            registry.add("storage.s3.endpoint") { localstack.getEndpointOverride(Service.S3).toString() }
            registry.add("storage.s3.region") { localstack.region }
            registry.add("storage.s3.access-key") { localstack.accessKey }
            registry.add("storage.s3.secret-key") { localstack.secretKey }
            registry.add("storage.s3.bucket") { "ark-local" }
        }
    }
}
