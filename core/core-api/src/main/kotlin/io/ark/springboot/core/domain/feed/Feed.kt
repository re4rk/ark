package io.ark.springboot.core.domain.feed

import io.ark.springboot.core.enums.feed.FeedCategory
import io.ark.springboot.core.enums.feed.FeedStatus
import java.time.LocalDateTime

data class Feed(
    val id: Long,
    val content: String,
    val isPublic: Boolean,
    val category: FeedCategory,
    val authorId: Long,
    val status: FeedStatus,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)
