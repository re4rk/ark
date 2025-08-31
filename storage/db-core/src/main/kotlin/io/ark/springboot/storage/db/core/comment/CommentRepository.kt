package io.ark.springboot.storage.db.core.comment

import com.querydsl.core.BooleanBuilder
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository

@Repository
class CommentRepository(
    private val commentJpaRepository: CommentJpaRepository,
) : QuerydslRepositorySupport(CommentEntity::class.java) {
    
    fun save(commentEntity: CommentEntity): CommentEntity = commentJpaRepository.save(commentEntity)

    fun findById(id: Long): CommentEntity? = commentJpaRepository.findById(id).orElse(null)

    fun findByFeedIdAndCursorAndLimit(feedId: Long, cursor: Long?, limit: Long): List<CommentEntity> {
        val c = QCommentEntity.commentEntity

        val whereClause = BooleanBuilder().apply {
            and(c.feedId.eq(feedId))
            and(c.isDeleted.eq(false))
            and(c.parentCommentId.isNull) // 최상위 댓글만 조회
            cursor?.let { and(c.id.lt(it)) }
        }

        return from(c)
            .where(whereClause)
            .limit(limit + 1)
            .orderBy(c.id.desc())
            .fetch()
    }

    fun findByParentCommentIdAndCursorAndLimit(parentCommentId: Long, cursor: Long?, limit: Long): List<CommentEntity> {
        val c = QCommentEntity.commentEntity

        val whereClause = BooleanBuilder().apply {
            and(c.parentCommentId.eq(parentCommentId))
            and(c.isDeleted.eq(false))
            cursor?.let { and(c.id.lt(it)) }
        }

        return from(c)
            .where(whereClause)
            .limit(limit + 1)
            .orderBy(c.id.desc())
            .fetch()
    }
}
