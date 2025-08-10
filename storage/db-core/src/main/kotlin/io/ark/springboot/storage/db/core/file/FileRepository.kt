package io.ark.springboot.storage.db.core.file

import org.springframework.data.jpa.repository.JpaRepository

interface FileRepository : JpaRepository<FileEntity, Long>
