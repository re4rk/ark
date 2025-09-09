package io.ark.springboot.storage.db.core.feed

import org.springframework.data.repository.Repository

interface FeedJpaRepository : Repository<FeedEntity, Long> {
    fun save(feedEntity: FeedEntity): FeedEntity
    fun findById(id: Long): FeedEntity?
}
