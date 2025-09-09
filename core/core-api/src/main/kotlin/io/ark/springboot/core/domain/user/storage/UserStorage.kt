package io.ark.springboot.core.domain.user.storage

import io.ark.springboot.core.domain.user.User
import io.ark.springboot.core.domain.user.UserData
import io.ark.springboot.core.enums.user.UserStatus
import io.ark.springboot.core.support.error.CoreException
import io.ark.springboot.core.support.error.ErrorType
import io.ark.springboot.storage.db.core.user.UserEntity
import io.ark.springboot.storage.db.core.user.UserRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class UserStorage(
    private val userRepository: UserRepository,
) {
    @Transactional
    fun save(userData: UserData): User {
        val entity = userData.toUserEntity()
        return userRepository.save(entity).toUser()
    }

    fun getUser(id: Long): User {
        return userRepository.findById(id)?.toUser()
            ?: throw CoreException(ErrorType.USER_NOT_FOUND)
    }

    fun getByEmail(email: String): User {
        return userRepository.findByEmail(email)?.toUser()
            ?: throw CoreException(ErrorType.USER_NOT_FOUND)
    }

    fun getByUsername(username: String): User {
        return userRepository.findByUsername(username)?.toUser()
            ?: throw CoreException(ErrorType.USER_NOT_FOUND)
    }

    fun existsByEmail(email: String): Boolean {
        return userRepository.existsByEmail(email)
    }

    fun existsByUsername(username: String): Boolean {
        return userRepository.existsByUsername(username)
    }

    @Transactional
    fun update(userId: Long, userData: UserData): User {
        val userEntity = userRepository.findById(userId)
            ?: throw CoreException(ErrorType.USER_NOT_FOUND)

        userEntity.email = userData.email
        userEntity.username = userData.username
        userEntity.password = userData.password
        userEntity.name = userData.name

        return userRepository.save(userEntity).toUser()
    }

    @Transactional
    fun delete(userId: Long) {
        val userEntity = userRepository.findById(userId)
            ?: throw CoreException(ErrorType.USER_NOT_FOUND)

        userEntity.status = UserStatus.DELETED
        userEntity.deletedAt = java.time.LocalDateTime.now()
    }

    companion object {
        private fun UserEntity.toUser() = User(
            id = id,
            email = email,
            username = username,
            password = password,
            name = name,
            phoneNumber = null, // UserEntity에 phoneNumber 필드가 없으므로 null
            isActive = status == UserStatus.ACTIVE,
            profileImageUrl = null, // UserEntity에 profileImageUrl 필드가 없으므로 null
        )

        private fun UserData.toUserEntity() = UserEntity(
            email = email,
            username = username,
            password = password,
            name = name,
        )
    }
}
