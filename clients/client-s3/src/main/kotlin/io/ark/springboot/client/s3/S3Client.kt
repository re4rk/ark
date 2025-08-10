package io.ark.springboot.client.s3

interface S3Client {
    fun upload(
        bytes: ByteArray,
        originalFilename: String,
        contentType: String? = null,
    ): StorageUploadResult

    fun getPresignedUrl(key: String, expirationMinutes: Long = 5): String

    fun exists(key: String): Boolean
}

data class StorageUploadResult(
    val key: String,
    val originalName: String,
    val size: Long,
    val mimeType: String,
    val uploadedAt: String,
    val downloadPath: String,
)
