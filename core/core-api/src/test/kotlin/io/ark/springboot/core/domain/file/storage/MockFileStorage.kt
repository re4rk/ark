package io.ark.springboot.core.domain.file.storage

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.UUID

@Service
@Profile("test")
class MockFileStorage : FileStorage {

    private val uploadedFiles = mutableMapOf<String, ByteArray>()

    override fun generateKey(uploaderId: String, category: String, originalName: String): String {
        return "mock/$category/$uploaderId/${UUID.randomUUID()}_$originalName"
    }

    override suspend fun upload(bytes: ByteArray, key: String, originalFilename: String, contentType: String) {
        uploadedFiles[key] = bytes
    }

    override fun exists(key: String): Boolean {
        return uploadedFiles.containsKey(key)
    }

    override fun getPresignedUrl(key: String): String {
        return if (uploadedFiles.containsKey(key)) {
            "http://localhost:8080/mock/files/$key"
        } else {
            throw IllegalArgumentException("File not found: $key")
        }
    }

    fun clear() {
        uploadedFiles.clear()
    }
}
