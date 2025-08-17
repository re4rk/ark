package io.ark.springboot.core.domain.file.storage

import io.ark.springboot.client.s3.S3Client
import org.springframework.stereotype.Service

@Service
class S3FileStorage(
    private val s3Client: S3Client,
) : FileStorage {

    override fun generateKey(uploaderId: String, category: String, originalName: String): String {
        return s3Client.generateKey(uploaderId, category, originalName)
    }

    override suspend fun upload(bytes: ByteArray, key: String, originalFilename: String, contentType: String) {
        s3Client.upload(bytes, key, originalFilename, contentType)
    }

    override fun exists(key: String): Boolean {
        return s3Client.exists(key)
    }

    override fun getPresignedUrl(key: String): String {
        return s3Client.getPresignedUrl(key)
    }
}
