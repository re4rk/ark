package io.ark.springboot.core.domain.common

data class Slice<T>(
    val content: List<T>,
    val lastCursor: Long,
    val hasNext: Boolean,
)
