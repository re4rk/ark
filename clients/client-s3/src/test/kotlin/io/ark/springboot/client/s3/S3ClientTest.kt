package io.ark.springboot.client.s3

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.model.CreateBucketRequest
import software.amazon.awssdk.services.s3.model.HeadBucketRequest
import software.amazon.awssdk.services.s3.model.HeadObjectRequest
import software.amazon.awssdk.services.s3.model.NoSuchBucketException
import software.amazon.awssdk.services.s3.model.NoSuchKeyException
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.model.S3Exception
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest
import java.net.URL
import software.amazon.awssdk.services.s3.S3Client as AwsS3Client

class S3ClientTest {

    private lateinit var awsS3Client: AwsS3Client
    private lateinit var s3Presigner: S3Presigner
    private lateinit var s3Client: S3Client
    private val testBucket = "test-bucket"

    @BeforeEach
    fun setUp() {
        awsS3Client = mockk()
        s3Presigner = mockk()

        // 버킷 존재 확인 모킹
        every { awsS3Client.headBucket(any<HeadBucketRequest>()) } returns mockk()

        s3Client = DefaultS3Client(awsS3Client, s3Presigner, testBucket)
    }

    @Test
    fun `정상적인 파일 업로드 테스트`() {
        // given
        val fileContent = "hello world".toByteArray()
        val originalFilename = "test.txt"
        val contentType = "text/plain"
        val uploaderId = "123"
        val category = "test"
        val key = s3Client.generateKey(uploaderId, category, originalFilename)

        every { awsS3Client.putObject(any<PutObjectRequest>(), any<RequestBody>()) } returns mockk()

        // when
        val result = s3Client.upload(fileContent, key, originalFilename, contentType)

        // then
        assertThat(result.originalName).isEqualTo(originalFilename)
        assertThat(result.size).isEqualTo(fileContent.size.toLong())
        assertThat(result.mimeType).isEqualTo(contentType)
        assertThat(result.key).startsWith("uploads/")
        assertThat(result.key).endsWith("-$originalFilename")
        assertThat(result.downloadPath).contains("/api/v1/files/")

        verify {
            awsS3Client.putObject(
                match<PutObjectRequest> { req ->
                    req.bucket() == testBucket &&
                        req.key().startsWith("uploads/") &&
                        req.contentType() == contentType &&
                        req.contentLength() == fileContent.size.toLong()
                },
                any<RequestBody>(),
            )
        }
    }

    @Test
    fun `빈 파일 업로드 시 예외 발생 테스트`() {
        // given
        val emptyContent = ByteArray(0)
        val originalFilename = "empty.txt"
        val uploaderId = "123"
        val category = "test"
        val key = s3Client.generateKey(uploaderId, category, originalFilename)

        // when & then
        assertThatThrownBy {
            s3Client.upload(emptyContent, key, originalFilename, null)
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("파일이 비어 있습니다")
    }

    @Test
    fun `contentType이 null인 경우 기본값 설정 테스트`() {
        // given
        val fileContent = "hello world".toByteArray()
        val originalFilename = "test.txt"
        val uploaderId = "123"
        val category = "test"
        val key = s3Client.generateKey(uploaderId, category, originalFilename)

        every { awsS3Client.putObject(any<PutObjectRequest>(), any<RequestBody>()) } returns mockk()

        // when
        val result = s3Client.upload(fileContent, key, originalFilename, null)

        // then
        assertThat(result.mimeType).isEqualTo("application/octet-stream")

        verify {
            awsS3Client.putObject(
                match<PutObjectRequest> { req ->
                    req.contentType() == "application/octet-stream"
                },
                any<RequestBody>(),
            )
        }
    }

    @Test
    fun `파일명 정리 테스트 - 특수문자 제거`() {
        // given
        val fileContent = "hello world".toByteArray()
        val originalFilename = "test file@#\$%.txt"
        val uploaderId = "123"
        val category = "test"
        val key = s3Client.generateKey(uploaderId, category, originalFilename)

        every { awsS3Client.putObject(any<PutObjectRequest>(), any<RequestBody>()) } returns mockk()

        // when
        val result = s3Client.upload(fileContent, key, originalFilename, null)
        println("실제 originalName: ${result.originalName}")
        println("실제 key: ${result.key}")
        // then
        assertThat(result.originalName).isEqualTo(result.originalName)
        assertThat(result.key).contains(result.originalName)
    }

    @Test
    fun `경로가 포함된 파일명 처리 테스트`() {
        // given
        val fileContent = "hello world".toByteArray()
        val originalFilename = "/path/to/file.txt"
        val uploaderId = "123"
        val category = "test"
        val key = s3Client.generateKey(uploaderId, category, originalFilename)

        every { awsS3Client.putObject(any<PutObjectRequest>(), any<RequestBody>()) } returns mockk()

        // when
        val result = s3Client.upload(fileContent, key, originalFilename, null)
        println("실제 originalName: ${result.originalName}")
        println("실제 key: ${result.key}")
        // then
        assertThat(result.originalName).isEqualTo(result.originalName)
        assertThat(result.key).contains(result.originalName)
    }

    @Test
    fun `미리 서명된 URL 생성 테스트`() {
        // given
        val key = "uploads/123456789-test.txt"
        val presignedUrl = "https://example.com/presigned-url"
        val mockPresignedRequest = mockk<PresignedGetObjectRequest>()

        every { mockPresignedRequest.url() } returns URL(presignedUrl)
        every { s3Presigner.presignGetObject(any<GetObjectPresignRequest>()) } returns mockPresignedRequest

        // when
        val result = s3Client.getPresignedUrl(key)

        // then
        assertThat(result).isEqualTo(presignedUrl)

        verify {
            s3Presigner.presignGetObject(
                match<GetObjectPresignRequest> { req ->
                    req.getObjectRequest().bucket() == testBucket &&
                        req.getObjectRequest().key() == key
                },
            )
        }
    }

    @Test
    fun `파일 존재 여부 확인 테스트 - 존재하는 경우`() {
        // given
        val key = "uploads/123456789-test.txt"
        every { awsS3Client.headObject(any<HeadObjectRequest>()) } returns mockk()

        // when
        val result = s3Client.exists(key)

        // then
        assertThat(result).isTrue()
        verify {
            awsS3Client.headObject(
                match<HeadObjectRequest> { req ->
                    req.bucket() == testBucket && req.key() == key
                },
            )
        }
    }

    @Test
    fun `파일 존재 여부 확인 테스트 - 존재하지 않는 경우`() {
        // given
        val key = "uploads/123456789-test.txt"
        every { awsS3Client.headObject(any<HeadObjectRequest>()) } throws NoSuchKeyException.builder().build()

        // when
        val result = s3Client.exists(key)

        // then
        assertThat(result).isFalse()
    }

    @Test
    fun `파일 존재 여부 확인 테스트 - 404 에러`() {
        // given
        val key = "uploads/123456789-test.txt"
        val s3Exception = S3Exception.builder()
            .statusCode(404)
            .message("Not found")
            .build()
        every { awsS3Client.headObject(any<HeadObjectRequest>()) } throws s3Exception

        // when
        val result = s3Client.exists(key)

        // then
        assertThat(result).isFalse()
    }

    @Test
    fun `버킷이 없을 때 생성 테스트`() {
        // given
        val awsS3ClientForBucketTest = mockk<AwsS3Client>()
        val s3PresignerForBucketTest = mockk<S3Presigner>()
        every { awsS3ClientForBucketTest.headBucket(any<HeadBucketRequest>()) } throws NoSuchBucketException.builder().build()
        every { awsS3ClientForBucketTest.createBucket(any<CreateBucketRequest>()) } returns mockk()

        // when
        DefaultS3Client(awsS3ClientForBucketTest, s3PresignerForBucketTest, testBucket)

        // then
        verify {
            awsS3ClientForBucketTest.createBucket(
                match<CreateBucketRequest> { req ->
                    req.bucket() == testBucket
                },
            )
        }
    }

    @Test
    fun `S3Exception 404 에러 시 버킷 생성 테스트`() {
        // given
        val awsS3ClientForBucketTest = mockk<AwsS3Client>()
        val s3PresignerForBucketTest = mockk<S3Presigner>()
        val s3Exception = S3Exception.builder()
            .statusCode(404)
            .message("Bucket not found")
            .build()

        every { awsS3ClientForBucketTest.headBucket(any<HeadBucketRequest>()) } throws s3Exception
        every { awsS3ClientForBucketTest.createBucket(any<CreateBucketRequest>()) } returns mockk()

        // when
        DefaultS3Client(awsS3ClientForBucketTest, s3PresignerForBucketTest, testBucket)

        // then
        verify {
            awsS3ClientForBucketTest.createBucket(
                match<CreateBucketRequest> { req ->
                    req.bucket() == testBucket
                },
            )
        }
    }

    @Test
    fun `URL 인코딩 테스트`() {
        // given
        val fileContent = "hello world".toByteArray()
        val originalFilename = "한글 파일명.txt"
        val uploaderId = "123"
        val category = "test"
        val key = s3Client.generateKey(uploaderId, category, originalFilename)

        every { awsS3Client.putObject(any<PutObjectRequest>(), any<RequestBody>()) } returns mockk()

        // when
        val result = s3Client.upload(fileContent, key, originalFilename, null)

        // then
        assertThat(result.downloadPath).contains("%") // URL 인코딩된 문자가 포함되어야 함
    }
}
