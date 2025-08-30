package io.ark.springboot.core.domain.common

data class Slice<T>(
    val data: List<T>,
    val offset: Int,
    val hasNext: Boolean,
)
