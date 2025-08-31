package io.ark.springboot.storage.db.core.feed

import io.ark.springboot.core.enums.feed.FeedCategory
import io.ark.springboot.core.enums.feed.FeedStatus
import io.ark.springboot.storage.db.core.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "feeds")
class FeedEntity(
    @Column(nullable = false, name = "content", columnDefinition = "TEXT")
    var content: String,

    @Column(nullable = false, name = "is_public")
    var isPublic: Boolean = true,

    @Column(nullable = false, name = "category") @Enumerated(EnumType.STRING)
    var category: FeedCategory,

    @Column(nullable = false, name = "author_id")
    var authorId: Long,

    @Column(nullable = false, name = "status") @Enumerated(EnumType.STRING)
    var status: FeedStatus = FeedStatus.ACTIVE,

    @Column(name = "deleted_at")
    var deletedAt: LocalDateTime? = null,
) : BaseEntity()
