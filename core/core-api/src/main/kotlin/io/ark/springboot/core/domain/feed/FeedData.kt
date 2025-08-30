package io.ark.springboot.core.domain.feed

data class FeedData(
    val content: String,
    val mediaUrls: List<String> = emptyList(),
    val hashtags: List<String> = emptyList(),
    val isPublic: Boolean = true,
    val category: FeedCategory,
    val authorId: Long,
)
