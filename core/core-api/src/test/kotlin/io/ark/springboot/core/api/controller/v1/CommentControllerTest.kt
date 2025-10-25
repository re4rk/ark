package io.ark.springboot.core.api.controller.v1

import io.ark.springboot.core.domain.feed.comment.Comment
import io.ark.springboot.core.domain.feed.comment.CommentData
import io.ark.springboot.core.domain.feed.comment.CommentService
import io.ark.springboot.test.api.RestDocsTest
import io.ark.springboot.test.api.RestDocsUtils.requestPreprocessor
import io.ark.springboot.test.api.RestDocsUtils.responsePreprocessor
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

class CommentControllerTest : RestDocsTest() {
    private lateinit var commentService: CommentService
    private lateinit var controller: CommentController

    @BeforeEach
    fun setUp() {
        commentService = mockk()
        controller = CommentController(commentService)
        mockMvc = mockController(controller)
    }

    private data class GIVEN(
        val content: String,
        val documentName: String,
        val authorId: Long = 1L,
        val feedId: Long = 1L,
        val commentId: Long = 1L,
        val parentCommentId: Long? = null,
    ) {
        fun createCommentData() = CommentData(
            content = content,
            authorId = authorId,
            feedId = feedId,
            parentCommentId = parentCommentId,
        )

        fun createComment(now: LocalDateTime = LocalDateTime.now()) = Comment(
            id = commentId,
            content = content,
            authorId = authorId,
            feedId = feedId,
            parentCommentId = parentCommentId,
            createdAt = now,
            updatedAt = now,
        )
    }

    @Test
    fun `댓글 생성 API 문서화`() {
        // given
        val testCases = listOf(
            GIVEN(
                content = "정말 좋은 글이네요! 👍 #좋아요",
                documentName = "comment-create-positive",
            ),
            GIVEN(
                content = "흥미로운 내용입니다. 더 자세히 설명해주세요.",
                documentName = "comment-create-question",
            ),
            GIVEN(
                content = "완전 공감합니다! 저도 같은 생각이에요 😊",
                documentName = "comment-create-agree",
            ),
        )

        testCases.forEach { given ->
            val commentData = given.createCommentData()
            val createdComment = given.createComment()

            every { commentService.createComment(any()) } returns createdComment
            every { commentService.getComment(createdComment.id) } returns createdComment

            // when & then
            given()
                .contentType(ContentType.JSON)
                .body(commentData)
                .post("/api/v1/comments")
                .then()
                .status(HttpStatus.OK)
                .apply(
                    document(
                        given.documentName,
                        requestPreprocessor(),
                        responsePreprocessor(),
                        requestFields(
                            "content" type STRING means "댓글 내용",
                            "authorId" type NUMBER means "작성자 ID",
                            "feedId" type NUMBER means "피드 ID",
                            "parentCommentId" type NUMBER means "부모 댓글 ID" isOptional true,
                        ),
                        responseFields(
                            "result" type STRING means "응답 결과",
                            "data" type NUMBER means "생성된 댓글 ID",
                            "error" type STRING isIgnored true,
                        ),
                    ),
                )
        }
    }

    @Test
    fun `댓글 수정 API 문서화`() {
        // given
        val testCases = listOf(
            GIVEN(
                content = "수정된 댓글 내용입니다. #수정",
                documentName = "comment-update-content",
                commentId = 1L,
            ),
            GIVEN(
                content = "오타를 수정했습니다. 더 정확한 정보로 업데이트!",
                documentName = "comment-update-typo",
                commentId = 2L,
            ),
        )

        testCases.forEach { given ->
            val commentData = given.createCommentData()
            val updatedComment = given.createComment(LocalDateTime.now())

            every { commentService.updateComment(given.commentId, any()) } returns updatedComment

            // when & then
            given()
                .contentType(ContentType.JSON)
                .body(commentData)
                .put("/api/v1/comments/${given.commentId}")
                .then()
                .status(HttpStatus.OK)
                .apply(
                    document(
                        given.documentName,
                        requestPreprocessor(),
                        responsePreprocessor(),
                        requestFields(
                            "content" type STRING means "댓글 내용",
                            "authorId" type NUMBER means "작성자 ID",
                            "feedId" type NUMBER means "피드 ID",
                            "parentCommentId" type NUMBER means "부모 댓글 ID" isOptional true,
                        ),
                        responseFields(
                            "result" type STRING means "응답 결과",
                            "data" type NUMBER means "수정된 댓글 ID",
                            "error" type STRING isIgnored true,
                        ),
                    ),
                )
        }
    }

    @Test
    fun `댓글 삭제 API 문서화`() {
        // given
        val testCases = listOf(
            GIVEN(
                content = "삭제될 댓글입니다.",
                documentName = "comment-delete",
                commentId = 1L,
            ),
        )

        testCases.forEach { given ->
            every { commentService.deleteComment(given.commentId) } returns Unit

            // when & then
            given()
                .delete("/api/v1/comments/${given.commentId}")
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
