package io.ark.springboot.storage.db.core.id

import io.ark.springboot.storage.db.core.id.SnowflakeIdGenerator.Companion.MAX_SEQUENCE
import org.hibernate.annotations.IdGeneratorType
import org.hibernate.engine.spi.SharedSessionContractImplementor
import org.hibernate.id.IdentifierGenerator
import java.io.Serializable
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * Hibernate 6+에서 사용할 수 있는 Snowflake ID 생성기
 */
@IdGeneratorType(SnowflakeIdGenerator::class)
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class SnowflakeId

class SnowflakeIdGenerator : IdentifierGenerator {

    private var workerId: Long = DEFAULT_WORKER_ID

    private var datacenterId: Long = DEFAULT_DATACENTER_ID
    private var sequence = 0L
    private var lastTimestamp = -1L
    override fun generate(session: SharedSessionContractImplementor?, `object`: Any?): Serializable {
        return nextId()
    }

    @Synchronized
    fun nextId(): Long {
        var timestamp = System.currentTimeMillis()

        if (timestamp < lastTimestamp) {
            throw RuntimeException("Clock moved backwards! Refusing to generate id for ${lastTimestamp - timestamp} milliseconds")
        }

        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) and MAX_SEQUENCE
            if (sequence == 0L) {
                timestamp = tilNextMillis(lastTimestamp)
            }
        } else {
            sequence = 0L
        }

        lastTimestamp = timestamp

        return ((timestamp - EPOCH) shl TIMESTAMP_LEFT_SHIFT.toInt()) or
            (datacenterId shl DATACENTER_ID_SHIFT.toInt()) or
            (workerId shl WORKER_ID_SHIFT.toInt()) or
            sequence
    }

    private fun tilNextMillis(lastTimestamp: Long): Long {
        var timestamp = System.currentTimeMillis()
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis()
        }
        return timestamp
    }

    /**
     * 설정을 통해 workerId와 datacenterId를 설정할 수 있음
     */
    fun configure(workerId: Long, datacenterId: Long) {
        require(workerId in 0..MAX_WORKER_ID) { "Worker ID must be between 0 and $MAX_WORKER_ID" }
        require(datacenterId in 0..MAX_DATACENTER_ID) { "Datacenter ID must be between 0 and $MAX_DATACENTER_ID" }

        this.workerId = workerId
        this.datacenterId = datacenterId
    }

    /**
     * ID에서 타임스탬프 추출
     */
    fun extractTimestamp(id: Long): LocalDateTime {
        val timestamp = (id shr TIMESTAMP_LEFT_SHIFT.toInt()) + EPOCH
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneOffset.UTC)
    }

    /**
     * ID에서 워커 ID 추출
     */
    fun extractWorkerId(id: Long): Long {
        return (id shr WORKER_ID_SHIFT.toInt()) and MAX_WORKER_ID
    }

    /**
     * ID에서 데이터센터 ID 추출
     */
    fun extractDatacenterId(id: Long): Long {
        return (id shr DATACENTER_ID_SHIFT.toInt()) and MAX_DATACENTER_ID
    }

    /**
     * ID에서 시퀀스 번호 추출
     */
    fun extractSequence(id: Long): Long {
        return id and MAX_SEQUENCE
    }

    companion object {
        const val EPOCH = 1288834974657L // 2010-11-04 01:42:54 UTC
        const val WORKER_ID_BITS = 5L
        const val DATACENTER_ID_BITS = 5L
        const val SEQUENCE_BITS = 12L

        const val MAX_WORKER_ID = (1L shl WORKER_ID_BITS.toInt()) - 1
        const val MAX_DATACENTER_ID = (1L shl DATACENTER_ID_BITS.toInt()) - 1
        const val MAX_SEQUENCE = (1L shl SEQUENCE_BITS.toInt()) - 1

        const val WORKER_ID_SHIFT = SEQUENCE_BITS
        const val DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS
        const val TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS

        // 기본값 설정 (설정 파일에서 읽어올 수 있음)
        const val DEFAULT_WORKER_ID = 1L
        const val DEFAULT_DATACENTER_ID = 1L
    }
}
