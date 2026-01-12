package com.cjbooms.fabrikt.servers.ktor

import com.example.multipart.controllers.FilesUploadController.Companion.filesUploadRoutes
import com.example.multipart.controllers.FilesUploadMultipleController.Companion.filesUploadMultipleRoutes
import com.example.multipart.controllers.ReceivedFile
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.jackson
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respondText
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class KtorMultipartServerTest {

    @Test
    fun `single file upload is parsed correctly with file metadata`() {
        val fileSlot = slot<ReceivedFile>()

        testApplication {
            configure()

            routing {
                filesUploadRoutes(FilesUploadControllerImpl(fileSlot))
            }

            val fileContent = "Hello, World!".toByteArray()

            val response = client.submitFormWithBinaryData(
                url = "/files/upload",
                formData = formData {
                    append("file", fileContent, Headers.build {
                        append(HttpHeaders.ContentDisposition, "filename=\"test.txt\"")
                        append(HttpHeaders.ContentType, "text/plain")
                    })
                }
            )

            assertEquals(HttpStatusCode.OK, response.status)
            assertThat(fileSlot.captured.content).isEqualTo(fileContent)
            assertThat(fileSlot.captured.originalFileName).isEqualTo("test.txt")
            assertThat(fileSlot.captured.contentType).isEqualTo(ContentType.Text.Plain)
            assertThat(response.bodyAsText()).contains("file-123")
        }
    }

    @Test
    fun `multiple files upload is parsed correctly with file metadata`() {
        val filesSlot = slot<List<ReceivedFile>>()

        testApplication {
            configure()

            routing {
                filesUploadMultipleRoutes(FilesUploadMultipleControllerImpl(filesSlot))
            }

            val file1Content = "File 1 content".toByteArray()
            val file2Content = "File 2 content".toByteArray()

            val response = client.submitFormWithBinaryData(
                url = "/files/upload-multiple",
                formData = formData {
                    append("files", file1Content, Headers.build {
                        append(HttpHeaders.ContentDisposition, "filename=\"file1.txt\"")
                        append(HttpHeaders.ContentType, "text/plain")
                    })
                    append("files", file2Content, Headers.build {
                        append(HttpHeaders.ContentDisposition, "filename=\"file2.pdf\"")
                        append(HttpHeaders.ContentType, "application/pdf")
                    })
                }
            )

            assertEquals(HttpStatusCode.OK, response.status)
            assertThat(filesSlot.captured).hasSize(2)
            assertThat(filesSlot.captured[0].content).isEqualTo(file1Content)
            assertThat(filesSlot.captured[0].originalFileName).isEqualTo("file1.txt")
            assertThat(filesSlot.captured[0].contentType).isEqualTo(ContentType.Text.Plain)
            assertThat(filesSlot.captured[1].content).isEqualTo(file2Content)
            assertThat(filesSlot.captured[1].originalFileName).isEqualTo("file2.pdf")
            assertThat(filesSlot.captured[1].contentType).isEqualTo(ContentType.Application.Pdf)
        }
    }

    @Test
    fun `single file upload returns bad request when required file is missing`() {
        val fileSlot = slot<ReceivedFile>()

        testApplication {
            configureWithStatusPages()

            routing {
                filesUploadRoutes(FilesUploadControllerImpl(fileSlot))
            }

            val response = client.submitFormWithBinaryData(
                url = "/files/upload",
                formData = formData {
                    append("other", "some value")
                }
            )

            assertEquals(HttpStatusCode.BadRequest, response.status)
            assertThat(response.bodyAsText()).contains("Missing required multipart part: file")
        }
    }

    private fun ApplicationTestBuilder.configure() {
        install(ContentNegotiation) {
            jackson()
        }
    }

    private fun ApplicationTestBuilder.configureWithStatusPages() {
        install(ContentNegotiation) {
            jackson()
        }
        install(StatusPages) {
            exception<io.ktor.server.plugins.BadRequestException> { call, cause ->
                call.respondText(cause.message ?: "Bad Request", status = HttpStatusCode.BadRequest)
            }
        }
    }
}
