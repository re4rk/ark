package io.ark.springboot.core.domain.user.validator

import io.ark.springboot.core.support.error.CoreException
import io.ark.springboot.core.support.error.ErrorType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class DefaultPasswordProcessor(
    private val passwordEncoder: PasswordEncoder,
) : PasswordProcessor {
    override fun validate(password: String) {
        // 최소 길이 검증
        if (password.length < 8) {
            throw CoreException(ErrorType.PASSWORD_TOO_SHORT)
        }

        // 최대 길이 검증
        if (password.length > 128) {
            throw CoreException(ErrorType.PASSWORD_TOO_LONG)
        }

        // 영문자 포함 검증
        if (!password.any { it.isLetter() }) {
            throw CoreException(ErrorType.PASSWORD_NO_LETTER)
        }

        // 숫자 포함 검증
        if (!password.any { it.isDigit() }) {
            throw CoreException(ErrorType.PASSWORD_NO_DIGIT)
        }

        // 특수문자 포함 검증
        if (!password.any { !it.isLetterOrDigit() }) {
            throw CoreException(ErrorType.PASSWORD_NO_SPECIAL)
        }
    }

    override fun encode(password: String): String {
        return passwordEncoder.encode(password)
    }

    override fun matches(password: String, encodedPassword: String): Boolean {
        return passwordEncoder.matches(password, encodedPassword)
    }
}
