# Snowflake ID Generator

## 개요
분산 환경에서 고유한 64비트 ID를 자동 생성하는 도구

## 특징
- **자동 생성**: JPA 엔티티에서 자동으로 ID 생성
- **고유성**: 분산 환경에서도 고유한 ID 보장
- **정렬 가능**: 시간 기반으로 정렬 가능

## 동작 원리

### 1. BaseEntity 구조
```kotlin
@MappedSuperclass
abstract class BaseEntity : Serializable {
    
    @Id
    @SnowflakeId  // Hibernate가 이 어노테이션을 감지하여 ID 생성
    var id: Long = 0
    
    // ... 다른 필드들
}
```

### 2. Hibernate ID 생성 과정
1. **엔티티 저장 시**: `repository.save(entity)` 호출
2. **ID 생성기 감지**: `@SnowflakeId` 어노테이션을 통해 `SnowflakeIdGenerator` 사용
3. **자동 ID 할당**: Hibernate가 `SnowflakeIdGenerator.nextId()` 호출하여 고유 ID 생성
4. **엔티티에 설정**: 생성된 ID를 `entity.id`에 자동 할당

### 3. 내부 동작 흐름
```
엔티티 저장 → @SnowflakeId 감지 → SnowflakeIdGenerator.nextId() → 64비트 ID 생성 → 엔티티에 할당
```

## 사용법

### 1. BaseEntity 상속
```kotlin
@Entity
class ExampleEntity(
    @Column
    var exampleColumn: String,
) : BaseEntity()
```

### 2. 자동 ID 생성
```kotlin
@Service
class ExampleService(
    private val exampleRepository: ExampleRepository
) {
    fun createExample(exampleColumn: String): ExampleEntity {
        val entity = ExampleEntity(exampleColumn)
        // entity.id는 여전히 0 (아직 ID 생성 전)
        val savedEntity = exampleRepository.save(entity)
        // savedEntity.id에는 자동 생성된 Snowflake ID가 설정됨
        return savedEntity
    }
}
```

## 설정
```yaml
snowflake:
  worker-id: 1
  datacenter-id: 1
```

## 구조
- **부호비트**: 1비트 (항상 0)
- **타임스탬프**: 41비트 (밀리초 단위)
- **데이터센터 ID**: 5비트 (0~31)
- **워커 ID**: 5비트 (0~31)
- **시퀀스**: 12비트 (0~4,095)

```aiignore
┌───────────────────────────────────────────────────────────────────────────────┐
│                               64 Bit                                          │
├───────────────────────────────────────────────────────────────────────────────┤
│  1 Bit  │  41 Bit   │   5 Bit   │  5 Bit  │       12 Bit      │               │
│    0    │ Timestamp │ Datacenter│  Worke  │      Sequence     │               │
│         │           │     ID    │   ID    │       Number      │               │
└───────────────────────────────────────────────────────────────────────────────┘
```
## 참고 자료

- [Twitter's Snowflake](https://github.com/twitter-archive/snowflake)
- [Snowflake ID Algorithm](https://en.wikipedia.org/wiki/Snowflake_ID)
- [Distributed ID Generation](https://engineering.fb.com/2017/02/20/core-data/generating-ids-at-scale/)
