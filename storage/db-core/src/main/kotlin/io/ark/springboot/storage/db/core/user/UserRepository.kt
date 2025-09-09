package io.ark.springboot.storage.db.core.user

import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport

class UserRepository(
    private val userJpaRepository: UserJpaRepository,
) : QuerydslRepositorySupport(UserEntity::class.java),
    UserJpaRepository by userJpaRepository
