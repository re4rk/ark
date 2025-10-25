package io.ark.springboot.storage.db.core.user

import org.springframework.data.repository.Repository

interface UserJpaRepository : Repository<UserEntity, Long> {
    fun save(userEntity: UserEntity): UserEntity
    fun findById(id: Long): UserEntity?
    fun findByEmail(email: String): UserEntity?
    fun findByUsername(username: String): UserEntity?
    fun existsByEmail(email: String): Boolean
    fun existsByUsername(username: String): Boolean
}
