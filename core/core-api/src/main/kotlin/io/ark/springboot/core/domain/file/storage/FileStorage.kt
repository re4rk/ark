package io.ark.springboot.core.domain.file.storage

interface FileStorage {
    fun generateKey(uploaderId: String, category: String, originalName: String): String
    suspend fun upload(bytes: ByteArray, key: String, originalFilename: String, contentType: String)
    fun exists(key: String): Boolean
    fun getPresignedUrl(key: String): String
}
