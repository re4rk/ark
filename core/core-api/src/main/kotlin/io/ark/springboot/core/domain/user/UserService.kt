package io.ark.springboot.core.domain.user

import io.ark.springboot.core.domain.user.storage.UserStorage
import io.ark.springboot.core.support.error.CoreException
import io.ark.springboot.core.support.error.ErrorType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class UserService(
    private val userStorage: UserStorage,
    private val passwordEncoder: PasswordEncoder,
) {
    @Transactional
    fun signUp(userData: UserData): User {
        if (userStorage.existsByEmail(userData.email)) {
            throw CoreException(ErrorType.USER_EMAIL_ALREADY_EXISTS)
        }

        if (userStorage.existsByUsername(userData.username)) {
            throw CoreException(ErrorType.USER_USERNAME_ALREADY_EXISTS)
        }

        val encodedPassword = passwordEncoder.encode(userData.password)
        val formattedUserData = userData.copy(password = encodedPassword)
        return userStorage.save(formattedUserData)
    }

    fun findByEmail(email: String): User {
        return userStorage.getByEmail(email)
    }

    fun findByUsername(username: String): User {
        return userStorage.getByUsername(username)
    }
}
