package io.ark.springboot.core.api.controller.v1

import io.ark.springboot.core.domain.feed.Feed
import io.ark.springboot.core.domain.feed.FeedData
import io.ark.springboot.core.domain.feed.FeedService
import io.ark.springboot.core.support.response.ApiResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/feeds")
class FeedController(
    private val feedService: FeedService,
) {
    @PostMapping
    fun createFeed(@RequestBody feedData: FeedData): ApiResponse<Feed> {
        val feed = feedService.createFeed(feedData)
        return ApiResponse.success(feed)
    }
}
