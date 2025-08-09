package io.ark.springboot.client.s3

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.model.CreateBucketRequest
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.HeadBucketRequest
import software.amazon.awssdk.services.s3.model.NoSuchBucketException
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.model.S3Exception
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.OffsetDateTime
import software.amazon.awssdk.services.s3.S3Client as AwsS3Client

@Component
class DefaultS3Client(
    private val awsS3Client: AwsS3Client,
    @Value("\${storage.s3.bucket}")
    private val bucket: String,
) : S3Client {

    init {
        ensureBucket()
    }

    override fun upload(
        bytes: ByteArray,
        originalFilename: String,
        contentType: String?,
    ): StorageUploadResult {
        require(bytes.isNotEmpty()) { "파일이 비어 있습니다" }

        val sanitizedOriginal = sanitizeFilename(originalFilename)
        val key = generateKey(sanitizedOriginal)

        val putReq = PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .contentType(contentType ?: "application/octet-stream")
            .contentLength(bytes.size.toLong())
            .metadata(
                mapOf(
                    "originalName" to sanitizedOriginal,
                    "contentType" to (contentType ?: "application/octet-stream"),
                    "size" to bytes.size.toString(),
                    "uploadedAt" to OffsetDateTime.now().toString(),
                ),
            )
            .build()

        awsS3Client.putObject(putReq, RequestBody.fromBytes(bytes))

        return StorageUploadResult(
            key = key,
            originalName = sanitizedOriginal,
            size = bytes.size.toLong(),
            mimeType = contentType ?: "application/octet-stream",
            uploadedAt = OffsetDateTime.now().toString(),
            downloadPath = "/api/v1/files/${urlEncode(key)}/download",
        )
    }

    override fun download(key: String): ByteArray {
        val req = GetObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .build()
        awsS3Client.getObjectAsBytes(req).also {
            return it.asByteArray()
        }
    }

    private fun ensureBucket() {
        try {
            awsS3Client.headBucket(HeadBucketRequest.builder().bucket(bucket).build())
        } catch (e: NoSuchBucketException) {
            awsS3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build())
        } catch (e: S3Exception) {
            if (e.statusCode() == 404) {
                awsS3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build())
            }
        }
    }

    private fun sanitizeFilename(name: String?): String {
        val base = name?.substringAfterLast('/')?.substringAfterLast('\\') ?: "unknown"
        return base.replace(Regex("[^A-Za-z0-9._-]"), "_")
    }

    private fun generateKey(originalName: String): String {
        val timestamp = System.currentTimeMillis()
        return "uploads/$timestamp-$originalName"
    }

    private fun urlEncode(value: String): String = URLEncoder.encode(value, StandardCharsets.UTF_8)
}
