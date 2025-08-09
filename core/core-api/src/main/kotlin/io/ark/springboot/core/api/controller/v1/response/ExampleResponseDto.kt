package io.ark.springboot.core.api.controller.v1.response

import io.ark.springboot.core.domain.ExampleResult

data class ExampleResponseDto(
    val id: Long,
    val result: String,
) {
    companion object {
        fun from(exampleResult: ExampleResult): ExampleResponseDto {
            return ExampleResponseDto(exampleResult.id, exampleResult.data)
        }
    }
}
