package io.ark.springboot.storage.db.core.feed

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FeedJpaRepository : JpaRepository<FeedEntity, Long>
