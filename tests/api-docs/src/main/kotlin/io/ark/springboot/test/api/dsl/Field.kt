package io.ark.springboot.test.api.dsl

import org.springframework.restdocs.payload.FieldDescriptor

/**
 * REST Docs FieldDescriptor를 래핑하는 DSL 클래스
 * Toss 기술 블로그 참조: https://toss.tech/article/kotlin-dsl-restdocs
 */
open class Field(
    val descriptor: FieldDescriptor,
) {
    val isIgnored: Boolean = descriptor.isIgnored
    val isOptional: Boolean = descriptor.isOptional

    open infix fun means(value: String): Field {
        descriptor.description(value)
        return this
    }

    open infix fun attributes(block: Field.() -> Unit): Field {
        block()
        return this
    }

    open infix fun withDefaultValue(value: String): Field {
        // 기본값 속성 추가 (필요시 확장)
        return this
    }

    open infix fun formattedAs(value: String): Field {
        // 포맷 속성 추가 (필요시 확장)
        return this
    }

    open infix fun example(value: String): Field {
        // 예시 속성 추가 (필요시 확장)
        return this
    }

    open infix fun isOptional(value: Boolean): Field {
        if (value) descriptor.optional()
        return this
    }

    open infix fun isIgnored(value: Boolean): Field {
        if (value) descriptor.ignored()
        return this
    }
}
