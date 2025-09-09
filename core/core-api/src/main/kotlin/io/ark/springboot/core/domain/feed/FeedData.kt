package io.ark.springboot.core.domain.feed

import io.ark.springboot.core.enums.feed.FeedCategory

data class FeedData(
    val content: String,
    val isPublic: Boolean = true,
    val category: FeedCategory,
    val authorId: Long,
)
