package io.ark.springboot.core.api.controller.v1

import io.ark.springboot.core.domain.common.Slice
import io.ark.springboot.core.domain.feed.Feed
import io.ark.springboot.core.domain.feed.FeedData
import io.ark.springboot.core.domain.feed.FeedService
import io.ark.springboot.core.domain.feed.comment.CommentService
import io.ark.springboot.core.enums.feed.FeedCategory
import io.ark.springboot.core.enums.feed.FeedStatus
import io.ark.springboot.test.api.RestDocsTest
import io.ark.springboot.test.api.RestDocsUtils.requestPreprocessor
import io.ark.springboot.test.api.RestDocsUtils.responsePreprocessor
import io.ark.springboot.test.api.dsl.BOOLEAN
import io.ark.springboot.test.api.dsl.DATETIME
import io.ark.springboot.test.api.dsl.ENUM
import io.ark.springboot.test.api.dsl.NUMBER
import io.ark.springboot.test.api.dsl.OBJECT
import io.ark.springboot.test.api.dsl.STRING
import io.ark.springboot.test.api.dsl.requestFields
import io.ark.springboot.test.api.dsl.responseFields
import io.ark.springboot.test.api.dsl.type
import io.mockk.every
import io.mockk.mockk
import io.restassured.http.ContentType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import java.time.LocalDateTime

class FeedControllerTest : RestDocsTest() {
    private lateinit var feedService: FeedService
    private lateinit var commentService: CommentService
    private lateinit var controller: FeedController

    @BeforeEach
    fun setUp() {
        feedService = mockk()
        commentService = mockk()
        controller = FeedController(feedService, commentService)
        mockMvc = mockController(controller)
    }

    private data class GIVEN(
        val content: String,
        val category: FeedCategory,
        val documentName: String,
        val isPublic: Boolean = true,
        val authorId: Long = 1L,
        val feedId: Long = 1L,
    ) {
        fun createFeedData() = FeedData(
            content = content,
            isPublic = isPublic,
            category = category,
            authorId = authorId,
        )

        fun createFeed(now: LocalDateTime = LocalDateTime.now()) = Feed(
            id = feedId,
            content = content,
            isPublic = isPublic,
            category = category,
            authorId = authorId,
            status = FeedStatus.ACTIVE,
            createdAt = now,
            updatedAt = now,
        )
    }

    @Test
    fun `피드 생성 API 문서화`() {
        // given
        val testCases = listOf(
            GIVEN(
                content = "안녕하세요! 오늘은 코딩 공부를 했습니다. #코딩 #개발",
                category = FeedCategory.TECH,
                documentName = "feed-create-tech",
            ),
            GIVEN(
                content = "오늘은 맛있는 커피를 마셨습니다 ☕ #커피 #라이프스타일",
                category = FeedCategory.LIFESTYLE,
                documentName = "feed-create-lifestyle",
                isPublic = false,
            ),
            GIVEN(
                content = "제주도 여행 중입니다! 🏝️ #제주도 #여행 #휴가",
                category = FeedCategory.TRAVEL,
                documentName = "feed-create-travel",
            ),
        )

        testCases.forEach { given ->
            val feedData = given.createFeedData()
            val createdFeed = given.createFeed()

            every { feedService.createFeed(any()) } returns createdFeed
            every { feedService.getFeed(given.feedId) } returns createdFeed

            // when & then
            given()
                .contentType(ContentType.JSON)
                .body(feedData)
                .post("/api/v1/feeds")
                .then()
                .status(HttpStatus.OK)
                .apply(
                    document(
                        given.documentName,
                        requestPreprocessor(),
                        responsePreprocessor(),
                        requestFields(
                            "content" type STRING means "피드 내용",
                            "isPublic" type BOOLEAN means "공개 여부" isOptional true,
                            "category" type ENUM(FeedCategory::class) means "피드 카테고리",
                            "authorId" type NUMBER means "작성자 ID",
                        ),
                        responseFields(
                            "result" type STRING means "응답 결과",
                            "data.id" type NUMBER means "피드 ID",
                            "data.content" type STRING means "피드 내용",
                            "data.isPublic" type BOOLEAN means "공개 여부",
                            "data.category" type STRING means "피드 카테고리",
                            "data.authorId" type NUMBER means "작성자 ID",
                            "data.status" type STRING means "피드 상태",
                            "data.createdAt" type DATETIME means "생성일시",
                            "data.updatedAt" type DATETIME means "수정일시",
                            "error" type STRING isIgnored true,
                        ),
                    ),
                )
        }
    }

    @Test
    fun `피드 단건조회 API 문서화`() {
        // given
        val testCases = listOf(
            GIVEN(
                content = "안녕하세요! 오늘은 코딩 공부를 했습니다. #코딩 #개발",
                category = FeedCategory.TECH,
                documentName = "feed-get-tech",
                feedId = 1L,
            ),
        )

        testCases.forEach { given ->
            val feed = given.createFeed()

            every { feedService.getFeed(given.feedId) } returns feed

            // when & then
            given()
                .get("/api/v1/feeds/${given.feedId}")
                .then()
                .status(HttpStatus.OK)
                .apply(
                    document(
                        given.documentName,
                        requestPreprocessor(),
                        responsePreprocessor(),
                        responseFields(
                            "result" type STRING means "응답 결과",
                            "data.id" type NUMBER means "피드 ID",
                            "data.content" type STRING means "피드 내용",
                            "data.isPublic" type BOOLEAN means "공개 여부",
                            "data.category" type STRING means "피드 카테고리",
                            "data.authorId" type NUMBER means "작성자 ID",
                            "data.status" type STRING means "피드 상태",
                            "data.createdAt" type DATETIME means "생성일시",
                            "data.updatedAt" type DATETIME means "수정일시",
                            "error" type STRING isIgnored true,
                        ),
                    ),
                )
        }
    }

    @Test
    fun `피드 목록조회 API 문서화`() {
        // given
        val testCases = listOf(
            GIVEN(
                content = "첫 번째 피드입니다. #첫번째",
                category = FeedCategory.TECH,
                documentName = "feed-list-first-page",
                feedId = 1L,
            ),
            GIVEN(
                content = "두 번째 피드입니다. #두번째",
                category = FeedCategory.LIFESTYLE,
                documentName = "feed-list-with-cursor",
                feedId = 2L,
            ),
        )

        testCases.forEach { given ->
            val feeds = listOf(
                given.createFeed(LocalDateTime.now().minusDays(1)),
                given.createFeed(LocalDateTime.now().minusDays(2)).copy(id = given.feedId + 1),
                given.createFeed(LocalDateTime.now().minusDays(3)).copy(id = given.feedId + 2),
            )

            val slice = Slice(content = feeds, hasNext = true, lastCursor = feeds.last().id)

            every { feedService.getFeeds(cursor = null) } returns slice
            every { feedService.getFeeds(cursor = given.feedId) } returns slice

            // when & then - 첫 페이지 조회
            given()
                .get("/api/v1/feeds")
                .then()
                .status(HttpStatus.OK)
                .apply(
                    document(
                        "${given.documentName}-first",
                        requestPreprocessor(),
                        responsePreprocessor(),
                        responseFields(
                            "result" type STRING means "응답 결과",
                            "data.content[].id" type NUMBER means "피드 ID",
                            "data.content[].content" type STRING means "피드 내용",
                            "data.content[].isPublic" type BOOLEAN means "공개 여부",
                            "data.content[].category" type STRING means "피드 카테고리",
                            "data.content[].authorId" type NUMBER means "작성자 ID",
                            "data.content[].status" type STRING means "피드 상태",
                            "data.content[].createdAt" type DATETIME means "생성일시",
                            "data.content[].updatedAt" type DATETIME means "수정일시",
                            "data.hasNext" type BOOLEAN means "다음 페이지 존재 여부",
                            "data.lastCursor" type NUMBER means "다음 페이지 커서" isOptional true,
                            "error" type STRING isIgnored true,
                        ),
                    ),
                )

            // when & then - 커서 기반 페이지네이션
            given()
                .queryParam("cursor", given.feedId)
                .get("/api/v1/feeds")
                .then()
                .status(HttpStatus.OK)
                .apply(
                    document(
                        "${given.documentName}-cursor",
                        requestPreprocessor(),
                        responsePreprocessor(),
                        responseFields(
                            "result" type STRING means "응답 결과",
                            "data.content[].id" type NUMBER means "피드 ID",
                            "data.content[].content" type STRING means "피드 내용",
                            "data.content[].isPublic" type BOOLEAN means "공개 여부",
                            "data.content[].category" type STRING means "피드 카테고리",
                            "data.content[].authorId" type NUMBER means "작성자 ID",
                            "data.content[].status" type STRING means "피드 상태",
                            "data.content[].createdAt" type DATETIME means "생성일시",
                            "data.content[].updatedAt" type DATETIME means "수정일시",
                            "data.hasNext" type BOOLEAN means "다음 페이지 존재 여부",
                            "data.lastCursor" type NUMBER means "다음 페이지 커서" isOptional true,
                            "error" type STRING isIgnored true,
                        ),
                    ),
                )
        }
    }

    @Test
    fun `피드 수정 API 문서화`() {
        // given
        val testCases = listOf(
            GIVEN(
                content = "수정된 피드 내용입니다. #수정 #업데이트",
                category = FeedCategory.TECH,
                documentName = "feed-update-tech",
                feedId = 1L,
            ),
        )

        testCases.forEach { given ->
            val feedData = given.createFeedData()
            val updatedFeed = given.createFeed(LocalDateTime.now())

            every { feedService.updateFeed(given.feedId, any()) } returns updatedFeed

            // when & then
            given()
                .contentType(ContentType.JSON)
                .body(feedData)
                .put("/api/v1/feeds/${given.feedId}")
                .then()
                .status(HttpStatus.OK)
                .apply(
                    document(
                        given.documentName,
                        requestPreprocessor(),
                        responsePreprocessor(),
                        requestFields(
                            "content" type STRING means "피드 내용",
                            "isPublic" type BOOLEAN means "공개 여부" isOptional true,
                            "category" type ENUM(FeedCategory::class) means "피드 카테고리",
                            "authorId" type NUMBER means "작성자 ID",
                        ),
                        responseFields(
                            "result" type STRING means "응답 결과",
                            "data.id" type NUMBER means "피드 ID",
                            "data.content" type STRING means "피드 내용",
                            "data.isPublic" type BOOLEAN means "공개 여부",
                            "data.category" type STRING means "피드 카테고리",
                            "data.authorId" type NUMBER means "작성자 ID",
                            "data.status" type STRING means "피드 상태",
                            "data.createdAt" type DATETIME means "생성일시",
                            "data.updatedAt" type DATETIME means "수정일시",
                            "error" type STRING isIgnored true,
                        ),
                    ),
                )
        }
    }

    @Test
    fun `피드 삭제 API 문서화`() {
        // given
        val testCases = listOf(
            GIVEN(
                content = "삭제될 피드입니다. #삭제",
                category = FeedCategory.TECH,
                documentName = "feed-delete-tech",
                feedId = 1L,
            ),
        )

        testCases.forEach { given ->
            every { feedService.deleteFeed(given.feedId) } returns Unit

            // when & then
            given()
                .delete("/api/v1/feeds/${given.feedId}")
                .then()
                .status(HttpStatus.OK)
                .apply(
                    document(
                        given.documentName,
                        requestPreprocessor(),
                        responsePreprocessor(),
                        responseFields(
                            "result" type STRING means "응답 결과",
                            "data" type OBJECT means "삭제 결과" isOptional true,
                            "error" type STRING isIgnored true,
                        ),
                    ),
                )
        }
    }
}
