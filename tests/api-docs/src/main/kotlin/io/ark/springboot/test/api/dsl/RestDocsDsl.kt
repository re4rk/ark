package io.ark.springboot.test.api.dsl

import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation

/**
 * REST Docs DSL의 핵심 infix 함수들
 * Toss 기술 블로그 참조: https://toss.tech/article/kotlin-dsl-restdocs
 */

/**
 * 기본 타입을 위한 infix 함수
 * 사용법: "fieldName" type STRING
 */
infix fun String.type(docsFieldType: DocsFieldType): Field {
    val field = createField(this, docsFieldType.type)
    when (docsFieldType) {
        is DATE -> field formattedAs "yyyy-MM-dd"
        is DATETIME -> field formattedAs "yyyy-MM-dd'T'HH:mm:ss"
        else -> {}
    }
    return field
}

/**
 * Enum 타입을 위한 infix 함수
 * 사용법: "fieldName" type ENUM(MyEnum::class)
 */
infix fun <T : Enum<T>> String.type(enumFieldType: ENUM<T>): Field {
    val field = createField(this, JsonFieldType.STRING, false)
    val enumValues = enumFieldType.enums.joinToString(", ") { it.name }
    field.descriptor.description("가능한 값: $enumValues")
    return field
}

/**
 * 필드 생성 헬퍼 함수
 */
private fun createField(value: String, type: JsonFieldType, optional: Boolean = false): Field {
    val descriptor = PayloadDocumentation.fieldWithPath(value)
        .type(type)
        .description("")

    if (optional) descriptor.optional()

    return Field(descriptor)
}

/**
 * 여러 필드를 한번에 정의하기 위한 DSL 함수
 */
fun fields(vararg fields: Field) = fields.map { it.descriptor }

/**
 * 요청 필드를 정의하기 위한 DSL 함수
 */
fun requestFields(vararg fields: Field) = PayloadDocumentation.requestFields(fields.map { it.descriptor })

/**
 * 응답 필드를 정의하기 위한 DSL 함수
 */
fun responseFields(vararg fields: Field) = PayloadDocumentation.responseFields(fields.map { it.descriptor })
