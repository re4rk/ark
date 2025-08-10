package io.ark.springboot.client.s3

import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.model.CreateBucketRequest
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.HeadBucketRequest
import software.amazon.awssdk.services.s3.model.HeadObjectRequest
import software.amazon.awssdk.services.s3.model.NoSuchBucketException
import software.amazon.awssdk.services.s3.model.NoSuchKeyException
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.model.S3Exception
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.time.OffsetDateTime
import software.amazon.awssdk.services.s3.S3Client as AwsS3Client

class DefaultS3Client(
    private val awsS3Client: AwsS3Client,
    private val s3Presigner: S3Presigner,
    private val bucket: String,
) : S3Client {

    init {
        ensureBucket()
    }

    override fun generateKey(uploaderId: String, category: String, originalFilename: String): String {
        val timestamp = System.currentTimeMillis()
        val sanitizedCategory = category.lowercase()
        val sanitizedOriginal = sanitizeFilename(originalFilename)
        return "uploads/$sanitizedCategory/$uploaderId/$timestamp-$sanitizedOriginal"
    }

    override fun upload(
        bytes: ByteArray,
        key: String,
        originalFilename: String,
        contentType: String?,
    ): StorageUploadResult {
        require(bytes.isNotEmpty()) { "파일이 비어 있습니다" }
        val sanitizedOriginal = sanitizeFilename(originalFilename)
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

    override fun exists(key: String): Boolean {
        val request = HeadObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .build()

        return try {
            awsS3Client.headObject(request)
            true
        } catch (e: NoSuchKeyException) {
            false
        } catch (e: S3Exception) {
            if (e.statusCode() == 404) false else throw e
        }
    }

    override fun getPresignedUrl(key: String, expiration: Duration): String {
        val getObjectRequest = GetObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .build()

        val presignRequest = GetObjectPresignRequest.builder()
            .signatureDuration(expiration)
            .getObjectRequest(getObjectRequest)
            .build()

        return s3Presigner.presignGetObject(presignRequest).url().toString()
    }

    override fun delete(key: String) {
        val deleteReq = DeleteObjectRequest.builder().bucket(bucket).key(key).build()
        awsS3Client.deleteObject(deleteReq)
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

    private fun urlEncode(value: String): String = URLEncoder.encode(value, StandardCharsets.UTF_8)

    private fun sanitizeFilename(name: String?): String {
        val base = name?.substringAfterLast('/')?.substringAfterLast('\\') ?: "unknown"
        return base.replace(Regex("[^A-Za-z0-9._-]"), "_")
    }
}
