package io.ark.springboot.client.s3

import java.time.Duration

interface S3Client {
    fun generateKey(uploaderId: String, category: String, originalFilename: String): String

    fun upload(
        bytes: ByteArray,
        key: String,
        originalFilename: String,
        contentType: String? = null,
    ): StorageUploadResult

    fun exists(key: String): Boolean

    fun getPresignedUrl(key: String, expiration: Duration = Duration.ofHours(1)): String

    fun delete(key: String)
}

data class StorageUploadResult(
    val key: String,
    val originalName: String,
    val size: Long,
    val mimeType: String,
    val uploadedAt: String,
    val downloadPath: String,
)
