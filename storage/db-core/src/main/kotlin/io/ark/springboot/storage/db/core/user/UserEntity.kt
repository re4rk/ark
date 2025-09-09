package io.ark.springboot.storage.db.core.user

import io.ark.springboot.core.enums.user.UserStatus
import io.ark.springboot.storage.db.core.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "users")
class UserEntity(
    @Column(nullable = false, name = "email", unique = true)
    var email: String,

    @Column(nullable = false, name = "username", unique = true)
    var username: String,

    @Column(nullable = false, name = "password")
    var password: String,

    @Column(nullable = false, name = "name")
    var name: String,

    @Column(nullable = false, name = "status") @Enumerated(EnumType.STRING)
    var status: UserStatus = UserStatus.ACTIVE,

    @Column(name = "deleted_at")
    var deletedAt: LocalDateTime? = null,
) : BaseEntity()
