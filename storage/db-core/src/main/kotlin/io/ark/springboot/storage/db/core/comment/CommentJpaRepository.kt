package io.ark.springboot.storage.db.core.comment

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CommentJpaRepository : JpaRepository<CommentEntity, Long>
