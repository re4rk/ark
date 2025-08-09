package io.ark.springboot.client.s3

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Configuration
import java.net.URI
import software.amazon.awssdk.services.s3.S3Client as AwsS3Client

@Configuration
@EnableConfigurationProperties(S3Properties::class)
class S3Config {

    @Bean
    fun awsS3Client(props: S3Properties): AwsS3Client {
        val httpClient = UrlConnectionHttpClient.builder().build()
        return AwsS3Client.builder()
            .httpClient(httpClient)
            .region(Region.of(props.region))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(props.accessKey, props.secretKey),
                ),
            )
            .serviceConfiguration(
                S3Configuration.builder()
                    .pathStyleAccessEnabled(true)
                    .build(),
            )
            .endpointOverride(URI.create(props.endpoint))
            .build()
    }

    @Bean
    fun s3Client(awsS3Client: AwsS3Client, props: S3Properties): S3Client = DefaultS3Client(awsS3Client, props.bucket)
}

@ConfigurationProperties(prefix = "storage.s3")
data class S3Properties(
    val endpoint: String,
    val region: String,
    val accessKey: String,
    val secretKey: String,
    val bucket: String,
)
