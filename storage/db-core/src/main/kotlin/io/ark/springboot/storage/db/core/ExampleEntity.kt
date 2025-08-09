package io.ark.springboot.storage.db.core

import jakarta.persistence.Column
import jakarta.persistence.Entity

@Entity
class ExampleEntity(
    @Column
    var exampleColumn: String,
) : BaseEntity()
