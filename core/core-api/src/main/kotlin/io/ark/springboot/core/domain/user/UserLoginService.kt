package io.ark.springboot.core.domain.user

import io.ark.springboot.core.domain.user.storage.UserStorage
import io.ark.springboot.core.domain.user.validator.PasswordProcessor
import io.ark.springboot.core.support.error.CoreException
import io.ark.springboot.core.support.error.ErrorType
import org.springframework.stereotype.Service

@Service
class UserLoginService(
    private val userStorage: UserStorage,
    private val passwordProcessor: PasswordProcessor,
) {
    fun login(email: String, password: String): User {
        val user = userStorage.getByEmail(email)

        if (!passwordProcessor.matches(password, user.encodedPassword)) {
            throw CoreException(ErrorType.PASSWORD_INVALID)
        }

        return user
    }
}
