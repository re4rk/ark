package io.ark.springboot.core.domain.feed.validator

import io.ark.springboot.core.support.error.CoreException
import io.ark.springboot.core.support.error.ErrorType
import org.springframework.stereotype.Component

// TODO: 외부 설정으로 관리할 수 있도록 개선
@Component
class FeedContentValidator(
    private val minLength: Int = 1,
    private val maxLength: Int = 1000,
    private val bannedWords: Set<String> = setOf("금지어1", "금지어2"),
) {
    fun validate(content: String) {
        if (content.length !in minLength..maxLength) {
            throw CoreException(ErrorType.CONTENT_INVALID_LENGTH)
        }
        bannedWords.forEach { banned ->
            if (content.contains(banned, ignoreCase = true)) {
                throw CoreException(ErrorType.CONTENT_BANNED_WORD)
            }
        }
    }
}
