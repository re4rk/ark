package io.ark.springboot.core.api.controller.v1.request

import io.ark.springboot.core.domain.ExampleData

data class ExampleRequestDto(
    val data: String,
) {
    fun toExampleData(): ExampleData {
        return ExampleData(data, data)
    }
}
