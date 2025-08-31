package io.ark.springboot.storage.db.core.feed

import com.querydsl.core.BooleanBuilder
import io.ark.springboot.core.enums.feed.FeedStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository

@Repository
interface FeedJpaRepository : JpaRepository<FeedEntity, Long>

@Repository
class FeedRepository(
    private val feedJpaRepository: FeedJpaRepository,
) : QuerydslRepositorySupport(FeedEntity::class.java) {
    fun save(feedEntity: FeedEntity): FeedEntity = feedJpaRepository.save(feedEntity)

    fun findById(id: Long): FeedEntity? = feedJpaRepository.findById(id).orElse(null)

    fun findByCursorAndLimit(cursor: Long?, limit: Long): List<FeedEntity> {
        val f = QFeedEntity.feedEntity

        val whereClause = BooleanBuilder().apply {
            cursor?.let { and(f.id.lt(it)) }
            and(f.status.eq(FeedStatus.ACTIVE))
        }

        return from(f)
            .where(whereClause)
            .limit(limit + 1)
            .orderBy(f.id.desc())
            .fetch()
    }
}
