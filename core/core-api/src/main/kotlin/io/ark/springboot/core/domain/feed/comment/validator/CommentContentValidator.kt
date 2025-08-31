package io.ark.springboot.core.domain.feed.comment.validator

import io.ark.springboot.core.domain.feed.comment.CommentData
import io.ark.springboot.core.support.error.CoreException
import io.ark.springboot.core.support.error.ErrorType
import org.springframework.stereotype.Component

// TODO: 외부 설정으로 관리할 수 있도록 개선
@Component
class CommentContentValidator(
    private val minLength: Int = 1,
    private val maxLength: Int = 1000,
    private val bannedWords: Set<String> = setOf("금지어1", "금지어2"),
) {
    fun validate(commentData: CommentData) {
        if (commentData.content.length !in minLength..maxLength) {
            throw CoreException(ErrorType.CONTENT_INVALID_LENGTH)
        }

        bannedWords.forEach { banned ->
            if (commentData.content.contains(banned, ignoreCase = true)) {
                throw CoreException(ErrorType.CONTENT_BANNED_WORD)
            }
        }

        if (commentData.feedId <= 0) {
            throw CoreException(ErrorType.INVALID_FEED_ID)
        }

        if (commentData.authorId <= 0) {
            throw CoreException(ErrorType.INVALID_AUTHOR_ID)
        }
    }
}
