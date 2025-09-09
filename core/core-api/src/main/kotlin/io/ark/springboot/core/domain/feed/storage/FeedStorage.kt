package io.ark.springboot.core.domain.feed.storage

import io.ark.springboot.core.domain.common.Slice
import io.ark.springboot.core.domain.feed.Feed
import io.ark.springboot.core.domain.feed.FeedData
import io.ark.springboot.core.enums.feed.FeedStatus
import io.ark.springboot.core.support.error.CoreException
import io.ark.springboot.core.support.error.ErrorType
import io.ark.springboot.storage.db.core.feed.FeedEntity
import io.ark.springboot.storage.db.core.feed.FeedRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
@Transactional(readOnly = true)
class FeedStorage(
    private val feedRepository: FeedRepository,
) {
    @Transactional
    fun save(feedData: FeedData): Feed {
        val entity = feedData.toFeedEntity()
        return feedRepository.save(entity).toFeed()
    }

    fun getFeed(id: Long): Feed = feedRepository.findById(id)?.toFeed()
        ?: throw CoreException(ErrorType.FEED_NOT_FOUND)

    fun getFeeds(cursor: Long?, limit: Long = 10L): Slice<Feed> = feedRepository
        .findByCursorAndLimit(cursor = cursor, limit = limit)
        .let {
            val feeds = it.take(limit.toInt()).map { feedEntity -> feedEntity.toFeed() }
            val lastCursor = feeds.lastOrNull()?.id ?: 0
            val hasNext = it.size == (limit + 1).toInt()
            Slice(content = feeds, lastCursor = lastCursor, hasNext = hasNext)
        }

    @Transactional
    fun update(feedId: Long, feedData: FeedData): Feed {
        val feedEntity = feedRepository.findById(feedId)
            ?: throw CoreException(ErrorType.FEED_NOT_FOUND)
        feedEntity.content = feedData.content
        feedEntity.isPublic = feedData.isPublic
        feedEntity.category = feedData.category
        return feedRepository.save(feedEntity).toFeed()
    }

    @Transactional
    fun delete(feedId: Long) {
        val feedEntity = feedRepository.findById(feedId)
            ?: throw CoreException(ErrorType.FEED_NOT_FOUND)
        feedEntity.status = FeedStatus.DELETED
        feedEntity.deletedAt = LocalDateTime.now()
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
