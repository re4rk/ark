package io.ark.springboot.core.domain.file.storage

import io.ark.springboot.client.s3.S3Client
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("!test")
class S3ExternalFileStorage(
    private val s3Client: S3Client,
) : ExternalFileStorage {

    override fun generateKey(uploaderId: String, category: String, originalName: String): String {
        return s3Client.generateKey(uploaderId, category, originalName)
    }

    override suspend fun upload(bytes: ByteArray, key: String, originalFilename: String, contentType: String) =
        withContext(Dispatchers.IO) {
            s3Client.upload(bytes, key, originalFilename, contentType)
            return@withContext
        }

    override fun exists(key: String): Boolean {
        return s3Client.exists(key)
    }

    override fun getPresignedUrl(key: String): String {
        return s3Client.getPresignedUrl(key)
    }
}
