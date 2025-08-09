package io.ark.springboot.client.s3

import org.springframework.stereotype.Component

interface S3Client {
    fun upload(
        bytes: ByteArray,
        originalFilename: String,
        contentType: String? = null,
    ): StorageUploadResult

    fun download(key: String): ByteArray
}

data class StorageUploadResult(
    val key: String,
    val originalName: String,
    val size: Long,
    val mimeType: String,
    val uploadedAt: String,
    val downloadPath: String,
)
