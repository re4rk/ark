package io.ark.springboot.storage.db.core.feed

import io.ark.springboot.core.enums.feed.FeedCategory
import io.ark.springboot.core.enums.feed.FeedStatus
import io.ark.springboot.storage.db.core.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table

@Entity
@Table(name = "feeds")
class FeedEntity(
    @Column(nullable = false, columnDefinition = "TEXT")
    var content: String,

    @Column(nullable = false)
    var isPublic: Boolean = true,

    @Column(nullable = false) @Enumerated(EnumType.STRING)
    var category: FeedCategory,

    @Column(nullable = false, name = "author_id")
    var authorId: Long,

    @Column(nullable = false) @Enumerated(EnumType.STRING)
    var status: FeedStatus = FeedStatus.ACTIVE,
) : BaseEntity()
