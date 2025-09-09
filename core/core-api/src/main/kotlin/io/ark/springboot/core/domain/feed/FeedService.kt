package io.ark.springboot.core.domain.feed

import io.ark.springboot.core.domain.common.Slice
import io.ark.springboot.core.domain.feed.storage.FeedStorage
import io.ark.springboot.core.domain.feed.validator.FeedContentValidator
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class FeedService(
    private val feedStorage: FeedStorage,
    private val feedContentValidator: FeedContentValidator,
) {

    @Transactional
    fun createFeed(feedData: FeedData): Feed {
        feedContentValidator.validate(feedData.content)
        val feed = feedStorage.save(feedData)
        // TODO: 1. 해시태그 파싱 및 정규화
        // TODO: 2. 해시태그 테이블에 저장
        return feed
    }

    fun getFeed(feedId: Long): Feed {
        val feed = feedStorage.getFeed(feedId)
        return feed
    }

    fun getFeeds(
        cursor: Long?,
    ): Slice<Feed> {
        val feeds = feedStorage.getFeeds(cursor)
        // TODO: 1. 작성자 정보 조회
        // TODO: 2. 좋아요 상태, 북마크 상태 조회
        return feeds
    }

    @Transactional
    fun updateFeed(feedId: Long, feedData: FeedData): Feed {
        feedContentValidator.validate(feedData.content)
        return feedStorage.update(feedId, feedData)
    }

    @Transactional
    fun deleteFeed(feedId: Long) {
        feedStorage.delete(feedId)
    }
}
