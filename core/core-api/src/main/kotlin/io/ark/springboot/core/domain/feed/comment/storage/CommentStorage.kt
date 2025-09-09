package io.ark.springboot.core.domain.feed.comment.storage

import io.ark.springboot.core.domain.common.Slice
import io.ark.springboot.core.domain.feed.comment.Comment
import io.ark.springboot.core.domain.feed.comment.CommentData
import io.ark.springboot.core.support.error.CoreException
import io.ark.springboot.core.support.error.ErrorType
import io.ark.springboot.storage.db.core.comment.CommentEntity
import io.ark.springboot.storage.db.core.comment.CommentRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
@Transactional(readOnly = true)
class CommentStorage(
    private val commentRepository: CommentRepository,
) {
    @Transactional
    fun save(commentData: CommentData): Comment {
        val entity = commentData.toCommentEntity()
        return commentRepository.save(entity).toComment()
    }

    fun getComment(commentId: Long): Comment = commentRepository.findById(commentId)?.toComment()
        ?: throw CoreException(ErrorType.COMMENT_NOT_FOUND)

    fun getCommentsByFeedId(feedId: Long, cursor: Long?, limit: Long = 10L): Slice<Comment> = commentRepository
        .findByFeedIdAndCursorAndLimit(feedId = feedId, cursor = cursor, limit = limit)
        .let {
            val comments =
                it.take(limit.toInt()).map { commentEntity -> commentEntity.toComment() }
            val lastCursor = comments.lastOrNull()?.id ?: 0
            val hasNext = it.size == (limit + 1).toInt()
            Slice(content = comments, lastCursor = lastCursor, hasNext = hasNext)
        }

    @Transactional
    fun update(commentId: Long, commentData: CommentData): Comment {
        val commentEntity = commentRepository.findById(commentId)
            ?: throw CoreException(ErrorType.COMMENT_NOT_FOUND)
        commentEntity.content = commentData.content
        return commentRepository.save(commentEntity).toComment()
    }

    @Transactional
    fun delete(commentId: Long) {
        val commentEntity = commentRepository.findById(commentId)
            ?: throw CoreException(ErrorType.COMMENT_NOT_FOUND)

        commentEntity.deletedAt = LocalDateTime.now()
    }

    companion object {
        private fun CommentEntity.toComment() = Comment(
            id = id,
            content = content,
            feedId = feedId,
            authorId = authorId,
            parentCommentId = parentCommentId,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

        private fun CommentData.toCommentEntity() = CommentEntity(
            content = content,
            feedId = feedId,
            authorId = authorId,
            parentCommentId = parentCommentId,
        )
    }
}
