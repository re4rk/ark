package io.ark.springboot.core.domain.feed.comment

import io.ark.springboot.core.domain.common.Slice
import io.ark.springboot.core.domain.feed.comment.storage.CommentStorage
import io.ark.springboot.core.domain.feed.comment.validator.CommentContentValidator
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class CommentService(
    private val commentStorage: CommentStorage,
    private val commentContentValidator: CommentContentValidator,
) {

    @Transactional
    fun createComment(commentData: CommentData): Comment {
        commentContentValidator.validate(commentData)
        return commentStorage.save(commentData)
    }

    fun getComment(commentId: Long): Comment {
        return commentStorage.getComment(commentId)
    }

    fun getCommentsByFeedId(feedId: Long, cursor: Long?, limit: Long = 10L): Slice<Comment> {
        return commentStorage.getCommentsByFeedId(feedId, cursor, limit)
    }

    @Transactional
    fun updateComment(commentId: Long, commentData: CommentData): Comment {
        commentContentValidator.validate(commentData)
        return commentStorage.update(commentId, commentData)
    }

    @Transactional
    fun deleteComment(commentId: Long) {
        commentStorage.delete(commentId)
    }
}
