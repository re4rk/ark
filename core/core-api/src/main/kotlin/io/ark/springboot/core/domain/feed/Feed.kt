package io.ark.springboot.core.domain.feed

import java.time.LocalDateTime

data class Feed(
    val id: Long,
    val content: String,
    val mediaUrls: List<String>,
    val hashtags: List<String>,
    val isPublic: Boolean,
    val category: FeedCategory,
    val authorId: Long,
    val status: FeedStatus,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)
