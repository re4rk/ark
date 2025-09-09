package io.ark.springboot.storage.db.core.comment

import io.ark.springboot.storage.db.core.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "comments")
class CommentEntity(
    @Column(nullable = false, name = "content", columnDefinition = "TEXT")
    var content: String,

    @Column(nullable = false, name = "feed_id")
    var feedId: Long,

    @Column(nullable = false, name = "author_id")
    var authorId: Long,

    @Column(name = "parent_comment_id")
    var parentCommentId: Long? = null,

    @Column(name = "deleted_at")
    var deletedAt: LocalDateTime? = null,
) : BaseEntity()
