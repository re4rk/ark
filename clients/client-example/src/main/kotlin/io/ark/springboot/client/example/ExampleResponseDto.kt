package io.ark.springboot.client.example

import io.ark.springboot.client.example.model.ExampleClientResult

internal data class ExampleResponseDto(
    val exampleResponseValue: String,
) {
    fun toResult(): ExampleClientResult {
        return ExampleClientResult(exampleResponseValue)
    }
}
