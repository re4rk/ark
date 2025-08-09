package io.ark.springboot.core.api.controller.v1

import io.ark.springboot.core.api.controller.v1.request.ExampleRequestDto
import io.ark.springboot.core.api.controller.v1.response.ExampleResponseDto
import io.ark.springboot.core.domain.ExampleData
import io.ark.springboot.core.domain.ExampleService
import io.ark.springboot.core.support.response.ApiResponse
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/examples")
@RestController
class ExampleController(
    val exampleService: ExampleService,
) {
    @PostMapping
    fun postExample(
        @RequestBody request: ExampleRequestDto,
    ): ApiResponse<ExampleResponseDto> {
        val result = exampleService.createExample(request.toExampleData())
        return ApiResponse.success(ExampleResponseDto.from(result))
    }

    @GetMapping("/{exampleId}")
    fun getExample(
        @PathVariable exampleId: Long,
    ): ApiResponse<ExampleResponseDto> {
        val result = exampleService.getExample(exampleId)
        return ApiResponse.success(ExampleResponseDto.from(result))
    }

    @PutMapping("/{exampleId}")
    fun putExample(
        @PathVariable exampleId: Long,
        @RequestBody request: ExampleRequestDto,
    ): ApiResponse<ExampleResponseDto> {
        val result = exampleService.updateExample(id = exampleId, request.toExampleData())
        return ApiResponse.success(ExampleResponseDto.from(result))
    }

    @DeleteMapping("/{exampleId}")
    fun deleteExample(
        @PathVariable exampleId: Long,
    ): ApiResponse<Unit> {
        exampleService.deleteExample(exampleId)
        return ApiResponse.success(Unit)
    }
}
