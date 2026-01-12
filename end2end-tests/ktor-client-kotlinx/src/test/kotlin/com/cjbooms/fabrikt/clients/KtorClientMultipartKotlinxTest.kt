package com.cjbooms.fabrikt.clients

import com.example.multipart.client.FileUpload
import com.example.multipart.client.FilesUploadClient
import com.example.multipart.client.FilesUploadMultipleClient
import com.example.multipart.client.NetworkResult
import com.example.multipart.models.UploadResponse
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.containing
import com.github.tomakehurst.wiremock.client.WireMock.ok
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertInstanceOf
import java.net.ServerSocket

/**
 * Tests for Ktor client multipart form-data uploads with kotlinx.serialization.
 *
 * Note: Ktor has a known bug where Content-Disposition parameters are sent unquoted
 * (e.g., `name=file` instead of `name="file"`). This is tracked in:
 * - https://github.com/ktorio/ktor/issues/1691
 * - https://github.com/ktorio/ktor/issues/5157
 *
 * The assertions in these tests use `containing("name=file")` (unquoted) to match
 * Ktor's actual behavior, even though the HTTP spec requires quoted values.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KtorClientMultipartKotlinxTest {
    private val port: Int = ServerSocket(0).use { socket -> socket.localPort }
    private val wiremock: WireMockServer = WireMockServer(options().port(port))
    private val json = Json { ignoreUnknownKeys = true }

    private fun createHttpClient() = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
        defaultRequest {
            url(wiremock.baseUrl())
        }
    }

    @BeforeEach
    fun setUp() {
        wiremock.start()
    }

    @AfterEach
    fun afterEach() {
        wiremock.resetAll()
        wiremock.stop()
    }

    @Test
    fun `single file upload sends multipart request correctly`() = runBlocking {
        val expectedResponse = UploadResponse(id = "file-123")

        wiremock.stubFor(
            post(urlEqualTo("/files/upload"))
                .willReturn(
                    ok()
                        .withHeader("Content-Type", "application/json")
                        .withBody(json.encodeToString(expectedResponse))
                )
        )

        val client = FilesUploadClient(createHttpClient())
        val fileContent = "Hello, World!".toByteArray()
        val result = client.uploadFile(
            file = FileUpload(fileContent)
        )

        assertInstanceOf<NetworkResult.Success<UploadResponse>>(result)

        wiremock.verify(
            postRequestedFor(urlEqualTo("/files/upload"))
                .withHeader("Content-Type", containing("multipart/form-data"))
                .withRequestBody(containing("name=file"))
                .withRequestBody(containing("Hello, World!"))
        )
    }

    @Test
    fun `multiple files upload sends multipart request correctly`() = runBlocking {
        val expectedResponse = UploadResponse(id = "file-multi")

        wiremock.stubFor(
            post(urlEqualTo("/files/upload-multiple"))
                .willReturn(
                    ok()
                        .withHeader("Content-Type", "application/json")
                        .withBody(json.encodeToString(expectedResponse))
                )
        )

        val client = FilesUploadMultipleClient(createHttpClient())
        val file1Content = "File 1 content".toByteArray()
        val file2Content = "File 2 content".toByteArray()

        val result = client.uploadMultipleFiles(
            files = listOf(FileUpload(file1Content), FileUpload(file2Content))
        )

        assertInstanceOf<NetworkResult.Success<UploadResponse>>(result)

        wiremock.verify(
            postRequestedFor(urlEqualTo("/files/upload-multiple"))
                .withHeader("Content-Type", containing("multipart/form-data"))
                .withRequestBody(containing("name=files"))
                .withRequestBody(containing("File 1 content"))
                .withRequestBody(containing("File 2 content"))
        )
    }

    @Test
    fun `single file upload uses custom filename when provided`() = runBlocking {
        val expectedResponse = UploadResponse(id = "file-custom")

        wiremock.stubFor(
            post(urlEqualTo("/files/upload"))
                .willReturn(
                    ok()
                        .withHeader("Content-Type", "application/json")
                        .withBody(json.encodeToString(expectedResponse))
                )
        )

        val client = FilesUploadClient(createHttpClient())
        val fileContent = "PNG image bytes".toByteArray()
        val result = client.uploadFile(
            file = FileUpload(fileContent, "my-image.png")
        )

        assertInstanceOf<NetworkResult.Success<UploadResponse>>(result)

        wiremock.verify(
            postRequestedFor(urlEqualTo("/files/upload"))
                .withHeader("Content-Type", containing("multipart/form-data"))
                .withRequestBody(containing("name=file"))
                .withRequestBody(containing("""filename="my-image.png""""))
                .withRequestBody(containing("PNG image bytes"))
        )
    }

    @Test
    fun `multiple files upload uses custom filenames when provided`() = runBlocking {
        val expectedResponse = UploadResponse(id = "file-multi-custom")

        wiremock.stubFor(
            post(urlEqualTo("/files/upload-multiple"))
                .willReturn(
                    ok()
                        .withHeader("Content-Type", "application/json")
                        .withBody(json.encodeToString(expectedResponse))
                )
        )

        val client = FilesUploadMultipleClient(createHttpClient())
        val file1Content = "PDF content 1".toByteArray()
        val file2Content = "PDF content 2".toByteArray()

        val result = client.uploadMultipleFiles(
            files = listOf(
                FileUpload(file1Content, "doc1.pdf"),
                FileUpload(file2Content, "doc2.pdf")
            )
        )

        assertInstanceOf<NetworkResult.Success<UploadResponse>>(result)

        wiremock.verify(
            postRequestedFor(urlEqualTo("/files/upload-multiple"))
                .withHeader("Content-Type", containing("multipart/form-data"))
                .withRequestBody(containing("name=files"))
                .withRequestBody(containing("""filename="doc1.pdf""""))
                .withRequestBody(containing("PDF content 1"))
                .withRequestBody(containing("""filename="doc2.pdf""""))
                .withRequestBody(containing("PDF content 2"))
        )
    }

    @Test
    fun `multiple files upload allows partial filename specification`() = runBlocking {
        val expectedResponse = UploadResponse(id = "file-partial")

        wiremock.stubFor(
            post(urlEqualTo("/files/upload-multiple"))
                .willReturn(
                    ok()
                        .withHeader("Content-Type", "application/json")
                        .withBody(json.encodeToString(expectedResponse))
                )
        )

        val client = FilesUploadMultipleClient(createHttpClient())
        val file1Content = "File 1".toByteArray()
        val file2Content = "File 2".toByteArray()

        // Only provide filename for first file, second uses default
        val result = client.uploadMultipleFiles(
            files = listOf(
                FileUpload(file1Content, "custom.txt"),
                FileUpload(file2Content)  // Uses default filename
            )
        )

        assertInstanceOf<NetworkResult.Success<UploadResponse>>(result)

        wiremock.verify(
            postRequestedFor(urlEqualTo("/files/upload-multiple"))
                .withHeader("Content-Type", containing("multipart/form-data"))
                .withRequestBody(containing("name=files"))
                .withRequestBody(containing("""filename="custom.txt""""))
                .withRequestBody(containing("File 1"))
                .withRequestBody(containing("File 2"))
        )
    }
}
