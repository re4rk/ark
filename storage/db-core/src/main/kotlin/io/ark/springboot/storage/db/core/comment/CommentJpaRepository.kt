package io.ark.springboot.storage.db.core.comment

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface CommentJpaRepository : JpaRepository<CommentEntity, Long> {
    
    @Query("SELECT COUNT(c) FROM CommentEntity c WHERE c.feedId = :feedId AND c.isDeleted = false")
    fun countByFeedIdAndIsDeletedFalse(@Param("feedId") feedId: Long): Long
    
    @Query("SELECT COUNT(c) FROM CommentEntity c WHERE c.authorId = :authorId AND c.isDeleted = false")
    fun countByAuthorIdAndIsDeletedFalse(@Param("authorId") authorId: Long): Long
}
