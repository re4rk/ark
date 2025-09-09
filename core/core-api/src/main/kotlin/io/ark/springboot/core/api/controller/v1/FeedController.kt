package io.ark.springboot.core.api.controller.v1

import io.ark.springboot.core.domain.common.Slice
import io.ark.springboot.core.domain.feed.Feed
import io.ark.springboot.core.domain.feed.FeedData
import io.ark.springboot.core.domain.feed.FeedService
import io.ark.springboot.core.domain.feed.comment.Comment
import io.ark.springboot.core.domain.feed.comment.CommentService
import io.ark.springboot.core.support.response.ApiResponse
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/feeds")
class FeedController(
    private val feedService: FeedService,
    private val commentService: CommentService,
) {
    @PostMapping
    fun createFeed(@RequestBody feedData: FeedData): ApiResponse<Long> {
        val feed = feedService.createFeed(feedData)
        return ApiResponse.success(feed.id)
    }

    @GetMapping("/{feedId}")
    fun getFeed(@PathVariable feedId: Long): ApiResponse<Feed> {
        return ApiResponse.success(feedService.getFeed(feedId))
    }

    @GetMapping
    fun getFeeds(@RequestParam(required = false) cursor: Long?): ApiResponse<Slice<Feed>> {
        return ApiResponse.success(feedService.getFeeds(cursor = cursor))
    }

    // TODO: 1. 권한 검증 (작성자만 수정 가능)
    @PutMapping("/{feedId}")
    fun updateFeed(
        @PathVariable feedId: Long,
        @RequestBody feedData: FeedData,
    ): ApiResponse<Long> {
        val feed = feedService.updateFeed(feedId, feedData)
        return ApiResponse.success(feed.id)
    }

    // TODO: 1. 권한 검증 (작성자만 삭제 가능)
    @DeleteMapping("/{feedId}")
    fun deleteFeed(@PathVariable feedId: Long): ApiResponse<Unit> {
        feedService.deleteFeed(feedId)
        return ApiResponse.success(Unit)
    }

    @GetMapping("/{feedId}/comments")
    fun getFeedComments(
        @PathVariable feedId: Long,
        @RequestParam(required = false) cursor: Long?,
    ): ApiResponse<Slice<Comment>> {
        val comments = commentService.getCommentsByFeedId(feedId, cursor)
        return ApiResponse.success(comments)
    }
}
