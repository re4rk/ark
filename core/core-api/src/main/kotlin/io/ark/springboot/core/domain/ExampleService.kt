package io.ark.springboot.core.domain

import io.ark.springboot.storage.db.core.ExampleEntity
import io.ark.springboot.storage.db.core.ExampleRepository
import org.springframework.stereotype.Service

@Service
class ExampleService(
    private val exampleRepository: ExampleRepository,
) {
    fun createExample(exampleData: ExampleData): ExampleResult {
        val a = exampleRepository.save(ExampleEntity(exampleColumn = exampleData.value))
        return ExampleResult(id = a.id, data = a.exampleColumn)
    }

    fun getExample(id: Long): ExampleResult {
        val a = exampleRepository.findById(id).orElseThrow { NoSuchElementException("Example not found") }
        return ExampleResult(id = a.id, data = a.exampleColumn)
    }

    fun updateExample(id: Long, exampleData: ExampleData): ExampleResult {
        val a = exampleRepository.findById(id).orElseThrow { NoSuchElementException("Example not found") }
        a.exampleColumn = exampleData.value
        val updated = exampleRepository.save(a)
        return ExampleResult(id = updated.id, data = updated.exampleColumn)
    }

    fun deleteExample(id: Long) {
        if (!exampleRepository.existsById(id)) {
            throw NoSuchElementException("Example not found")
        }
        exampleRepository.deleteById(id)
    }
}
