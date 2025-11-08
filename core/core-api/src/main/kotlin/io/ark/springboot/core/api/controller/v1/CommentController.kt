package io.ark.springboot.core.api.controller.v1

import io.ark.springboot.core.domain.feed.comment.CommentData
import io.ark.springboot.core.domain.feed.comment.CommentService
import io.ark.springboot.core.support.response.ApiResponse
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/comments")
class CommentController(
    private val commentService: CommentService,
) {
    @PostMapping
    fun createComment(@RequestBody commentData: CommentData): ApiResponse<Long> {
        val comment = commentService.createComment(commentData)
        return ApiResponse.success(comment.id)
    }

    @PutMapping("/{commentId}")
    fun updateComment(@PathVariable commentId: Long, @RequestBody commentData: CommentData): ApiResponse<Long> {
        val comment = commentService.updateComment(commentId, commentData)
        return ApiResponse.success(comment.id)
    }

    @DeleteMapping("/{commentId}")
    fun deleteComment(@PathVariable commentId: Long): ApiResponse<Unit> {
        commentService.deleteComment(commentId)
        return ApiResponse.success(Unit)
    }
}
