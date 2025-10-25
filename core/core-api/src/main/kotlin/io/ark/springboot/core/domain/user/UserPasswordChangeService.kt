package io.ark.springboot.core.domain.user

import io.ark.springboot.core.domain.user.storage.UserStorage
import io.ark.springboot.core.domain.user.validator.PasswordProcessor
import io.ark.springboot.core.support.error.CoreException
import io.ark.springboot.core.support.error.ErrorType
import org.springframework.stereotype.Service

@Service
class UserPasswordChangeService(
    private val userStorage: UserStorage,
    private val passwordProcessor: PasswordProcessor,
) {
    fun changePassword(email: String, password: String, newPassword: String) {
        val user = userStorage.getByEmail(email)

        passwordProcessor.validate(newPassword)

        if (!passwordProcessor.matches(password, user.encodedPassword)) {
            throw CoreException(ErrorType.PASSWORD_INVALID)
        }

        userStorage.update(user.id, UserData.from(user).copy(password = newPassword))
    }
}
