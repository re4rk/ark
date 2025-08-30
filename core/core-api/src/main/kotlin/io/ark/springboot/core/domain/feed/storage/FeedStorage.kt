package io.ark.springboot.core.domain.feed.storage

import io.ark.springboot.core.domain.feed.Feed
import io.ark.springboot.core.domain.feed.FeedData
import io.ark.springboot.storage.db.core.feed.FeedEntity
import io.ark.springboot.storage.db.core.feed.FeedJpaRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class FeedStorage(
    private val feedJpaRepository: FeedJpaRepository,
) {
    @Transactional
    fun save(feedData: FeedData): Feed {
        val entity = feedData.toFeedEntity()
        return feedJpaRepository.save(entity).toFeed()
    }

    companion object {
        private fun FeedEntity.toFeed() = Feed(
            id = id,
            content = content,
            isPublic = isPublic,
            category = category,
            authorId = authorId,
            status = status,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

        private fun FeedData.toFeedEntity() = FeedEntity(
            content = content,
            isPublic = isPublic,
            category = category,
            authorId = authorId,
        )
    }
}
