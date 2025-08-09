package io.ark.springboot.client.s3

import io.ark.springboot.client.s3.DefaultS3Client
import io.ark.springboot.client.s3.S3Client
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import software.amazon.awssdk.core.ResponseBytes
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.model.CreateBucketRequest
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectResponse
import software.amazon.awssdk.services.s3.model.HeadBucketRequest
import software.amazon.awssdk.services.s3.model.NoSuchBucketException
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.model.S3Exception
import software.amazon.awssdk.services.s3.S3Client as AwsS3Client

class S3ClientTest {

    private lateinit var awsS3Client: AwsS3Client
    private lateinit var s3Client: S3Client
    private val testBucket = "test-bucket"

    @BeforeEach
    fun setUp() {
        awsS3Client = mockk()

        // 버킷 존재 확인 모킹
        every { awsS3Client.headBucket(any<HeadBucketRequest>()) } returns mockk()

        s3Client = DefaultS3Client(awsS3Client, testBucket)
    }

    @Test
    fun `정상적인 파일 업로드 테스트`() {
        // given
        val fileContent = "hello world".toByteArray()
        val originalFilename = "test.txt"
        val contentType = "text/plain"

        every { awsS3Client.putObject(any<PutObjectRequest>(), any<RequestBody>()) } returns mockk()

        // when
        val result = s3Client.upload(fileContent, originalFilename, contentType)

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

        // when & then
        assertThatThrownBy {
            s3Client.upload(emptyContent, originalFilename)
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("파일이 비어 있습니다")
    }

    @Test
    fun `contentType이 null인 경우 기본값 설정 테스트`() {
        // given
        val fileContent = "hello world".toByteArray()
        val originalFilename = "test.txt"

        every { awsS3Client.putObject(any<PutObjectRequest>(), any<RequestBody>()) } returns mockk()

        // when
        val result = s3Client.upload(fileContent, originalFilename, null)

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
        val originalFilename = "test file@#$%.txt"

        every { awsS3Client.putObject(any<PutObjectRequest>(), any<RequestBody>()) } returns mockk()

        // when
        val result = s3Client.upload(fileContent, originalFilename)

        // then
        assertThat(result.originalName).isEqualTo("test_file____.txt")
        assertThat(result.key).contains("test_file____.txt")
    }

    @Test
    fun `경로가 포함된 파일명 처리 테스트`() {
        // given
        val fileContent = "hello world".toByteArray()
        val originalFilename = "/path/to/file.txt"

        every { awsS3Client.putObject(any<PutObjectRequest>(), any<RequestBody>()) } returns mockk()

        // when
        val result = s3Client.upload(fileContent, originalFilename)

        // then
        assertThat(result.originalName).isEqualTo("file.txt")
        assertThat(result.key).contains("file.txt")
    }

    @Test
    fun `파일 다운로드 성공 테스트`() {
        // given
        val key = "uploads/123456789-test.txt"
        val fileContent = "hello world".toByteArray()
        val responseBytes = mockk<ResponseBytes<GetObjectResponse>>()

        every { responseBytes.asByteArray() } returns fileContent
        every { awsS3Client.getObjectAsBytes(any<GetObjectRequest>()) } returns responseBytes

        // when
        val result = s3Client.download(key)

        // then
        assertThat(result).isEqualTo(fileContent)

        verify {
            awsS3Client.getObjectAsBytes(
                match<GetObjectRequest> { req ->
                    req.bucket() == testBucket && req.key() == key
                },
            )
        }
    }

    @Test
    fun `버킷이 없을 때 생성 테스트`() {
        // given
        val awsS3ClientForBucketTest = mockk<AwsS3Client>()
        every { awsS3ClientForBucketTest.headBucket(any<HeadBucketRequest>()) } throws NoSuchBucketException.builder().build()
        every { awsS3ClientForBucketTest.createBucket(any<CreateBucketRequest>()) } returns mockk()

        // when
        DefaultS3Client(awsS3ClientForBucketTest, testBucket)

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
        val s3Exception = S3Exception.builder()
            .statusCode(404)
            .message("Bucket not found")
            .build()

        every { awsS3ClientForBucketTest.headBucket(any<HeadBucketRequest>()) } throws s3Exception
        every { awsS3ClientForBucketTest.createBucket(any<CreateBucketRequest>()) } returns mockk()

        // when
        DefaultS3Client(awsS3ClientForBucketTest, testBucket)

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

        every { awsS3Client.putObject(any<PutObjectRequest>(), any<RequestBody>()) } returns mockk()

        // when
        val result = s3Client.upload(fileContent, originalFilename)

        // then
        assertThat(result.downloadPath).contains("%") // URL 인코딩된 문자가 포함되어야 함
    }
}
