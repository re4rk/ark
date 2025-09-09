package io.ark.springboot.core.domain.feed.comment

import java.time.LocalDateTime

data class Comment(
    val id: Long,
    val content: String,
    val feedId: Long,
    val authorId: Long,
    val parentCommentId: Long? = null,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)
