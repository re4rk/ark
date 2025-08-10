package io.ark.springboot.storage.db.core

import io.ark.springboot.storage.db.core.id.SnowflakeId
import jakarta.persistence.Column
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@MappedSuperclass
abstract class BaseEntity {
    @Id @SnowflakeId
    var id: Long = 0

    @Column(nullable = false, updatable = false, name = "created_at") @CreationTimestamp
    val createdAt: LocalDateTime = LocalDateTime.MIN

    @Column(nullable = false, name = "updated_at") @UpdateTimestamp
    val updatedAt: LocalDateTime = LocalDateTime.MIN
}
