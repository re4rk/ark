package io.ark.springboot.test.api.dsl

import org.springframework.restdocs.payload.JsonFieldType
import kotlin.reflect.KClass

/**
 * REST Docs DSL을 위한 필드 타입 정의
 * Toss 기술 블로그 참조: https://toss.tech/article/kotlin-dsl-restdocs
 */
open class DocsFieldType(val type: JsonFieldType)

// 기본 타입들
object STRING : DocsFieldType(JsonFieldType.STRING)
object NUMBER : DocsFieldType(JsonFieldType.NUMBER)
object BOOLEAN : DocsFieldType(JsonFieldType.BOOLEAN)
object OBJECT : DocsFieldType(JsonFieldType.OBJECT)
object ARRAY : DocsFieldType(JsonFieldType.ARRAY)
object NULL : DocsFieldType(JsonFieldType.NULL)

// 확장 타입들
object DATE : DocsFieldType(JsonFieldType.STRING)
object DATETIME : DocsFieldType(JsonFieldType.STRING)

/**
 * Enum 타입을 위한 특별한 DocsFieldType
 */
data class ENUM<T : Enum<T>>(val enums: Collection<T>) : DocsFieldType(JsonFieldType.STRING) {
    constructor(clazz: KClass<T>) : this(clazz.java.enumConstants.asList())
}
