package io.ark.springboot.storage.db.core.comment

import org.springframework.data.repository.Repository

interface CommentJpaRepository : Repository<CommentEntity, Long> {
    fun save(commentEntity: CommentEntity): CommentEntity
    fun findById(id: Long): CommentEntity?
}
