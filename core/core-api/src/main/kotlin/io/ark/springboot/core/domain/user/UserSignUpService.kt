package io.ark.springboot.core.domain.user

import io.ark.springboot.core.domain.user.storage.UserStorage
import io.ark.springboot.core.domain.user.validator.PasswordProcessor
import io.ark.springboot.core.support.error.CoreException
import io.ark.springboot.core.support.error.ErrorType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserSignUpService(
    private val userStorage: UserStorage,
    private val passwordValidator: PasswordProcessor,
) {
    @Transactional
    fun signUp(userData: UserData): User {
        if (userStorage.existsByEmail(userData.email)) {
            throw CoreException(ErrorType.USER_EMAIL_ALREADY_EXISTS)
        }

        if (userStorage.existsByUsername(userData.username)) {
            throw CoreException(ErrorType.USER_USERNAME_ALREADY_EXISTS)
        }

        passwordValidator.validate(userData.password)

        return userStorage.save(userData)
    }
}
