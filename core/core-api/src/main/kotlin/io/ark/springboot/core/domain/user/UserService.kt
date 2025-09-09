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
        // 이메일 중복 확인
        if (userStorage.existsByEmail(userData.email)) {
            throw CoreException(ErrorType.USER_EMAIL_ALREADY_EXISTS)
        }

        // 사용자명 중복 확인
        if (userStorage.existsByUsername(userData.username)) {
            throw CoreException(ErrorType.USER_USERNAME_ALREADY_EXISTS)
        }

        // 비밀번호 암호화
        val encodedPassword = passwordEncoder.encode(userData.password)

        // 사용자 데이터 생성
        val formattedUserData = userData.copy(password = encodedPassword)

        // 사용자 저장
        val savedUser = userStorage.save(formattedUserData)

        // 응답 DTO 생성
        return savedUser
    }

    fun findByEmail(email: String): User {
        return userStorage.getByEmail(email)
    }

    fun findByUsername(username: String): User {
        return userStorage.getByUsername(username)
    }
}
