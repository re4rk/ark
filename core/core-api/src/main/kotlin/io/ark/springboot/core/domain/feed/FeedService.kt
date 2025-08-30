package io.ark.springboot.core.domain.feed

import io.ark.springboot.core.domain.common.Slice
import io.ark.springboot.core.domain.feed.storage.FeedStorage
import io.ark.springboot.core.domain.feed.validator.FeedContentValidator
import io.ark.springboot.core.enums.feed.FeedCategory
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

    fun getFeeds(
        offset: Int,
        size: Int,
        sort: String = "latest",
        category: FeedCategory? = null,
        hashtag: String? = null,
    ): Slice<Feed> {
        // TODO: 실제 구현 예정
        // 1. 피드 목록 조회 (offset 기반 슬라이드)
        // 2. 작성자 정보 조회
        // 3. 좋아요 상태, 북마크 상태 조회
        // 4. hasNext, nextOffset 계산
        throw NotImplementedError("구현 예정")
    }

    fun getFeed(feedId: Long): Feed {
        // TODO: 실제 구현 예정
        // 1. 피드 조회
        // 2. 댓글 목록 조회
        throw NotImplementedError("구현 예정")
    }

    @Transactional
    fun updateFeed(feedId: Long, feedData: FeedData): Feed {
        // TODO: 실제 구현 예정
        // 1. 권한 검증 (작성자만 수정 가능)
        // 2. 피드 업데이트
        throw NotImplementedError("구현 예정")
    }

    @Transactional
    fun deleteFeed(feedId: Long) {
        // TODO: 실제 구현 예정
        // 1. 권한 검증 (작성자만 삭제 가능)
        // 2. 피드 삭제 (soft delete)
        throw NotImplementedError("구현 예정")
    }
}
