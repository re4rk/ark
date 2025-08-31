package io.ark.springboot.core.domain.feed.comment

data class CommentData(
    val content: String,
    val feedId: Long,
    val authorId: Long,
    val parentCommentId: Long? = null,
)
