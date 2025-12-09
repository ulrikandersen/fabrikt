package com.cjbooms.fabrikt.clients

import com.example.client.CatalogsItemsClient
import com.example.client.CatalogsSearchClient
import com.example.client.NetworkError
import com.example.client.NetworkResult
import com.example.client.NoContentClient
import com.example.models.Item
import com.example.models.SortOrder
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.common.ConsoleNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.options
import com.marcinziolo.kotlin.wiremock.get
import com.marcinziolo.kotlin.wiremock.like
import com.marcinziolo.kotlin.wiremock.post
import com.marcinziolo.kotlin.wiremock.returns
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.testing.testApplication
import io.mockk.slot
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertInstanceOf
import java.net.ServerSocket
import kotlin.test.assertEquals
import kotlin.test.fail

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KtorClientKotlinxTest {

    private val port: Int = ServerSocket(0).use { socket -> socket.localPort }

    private val wiremock: WireMockServer = WireMockServer(options().port(port).notifier(ConsoleNotifier(true)))

    init {
        wiremock.start()
    }

    @BeforeEach
    fun setUp() {
        wiremock.resetAll()
    }

    private fun createHttpClient() = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
        defaultRequest {
            url(wiremock.baseUrl())
        }
    }

    @Nested
    inner class Client {
        @Test
        fun `client performs post with body`() {
            wiremock.post {
                urlPath like "/catalogs/catalog-a/items"
            } returns {
                statusCode = 201
                body = """
                    {
                        "id": "id-1",
                        "name": "item-a",
                        "description": "description-a",
                        "price": 123.45
                    }
                """.trimIndent()
                header = "Content-Type" to "application/json"
            }

            val client = CatalogsItemsClient(createHttpClient())

            runBlocking {
                val result = client.createItem(
                    item = Item(
                        id = "id-1",
                        name = "item-a",
                        description = "description-a",
                        price = 123.45
                    ),
                    catalogId = "catalog-a",
                    randomNumber = 123,
                    xRequestID = "request-id"
                )

                when (result) {
                    is NetworkResult.Success -> {
                        println("Created item with name: ${result.data.name}")
                    }

                    is NetworkResult.Failure -> {
                        fail("Failed to create item. Error: ${result.error}")
                    }
                }
            }
        }

        @Test
        fun `client performs post and gets 404 back`() {
            wiremock.post {
                urlPath like "/catalogs/catalog-a/items"
            } returns {
                statusCode = 404
                body = "Not found"
            }

            val client = CatalogsItemsClient(createHttpClient())

            runBlocking {
                val result = client.createItem(
                    item = Item(
                        id = "id-1",
                        name = "item-a",
                        description = "description-a",
                        price = 123.45
                    ),
                    catalogId = "catalog-a",
                    randomNumber = 123,
                    xRequestID = "request-id"
                )

                when (result) {
                    is NetworkResult.Success -> {
                        fail("Expected 404 but got success")
                    }

                    is NetworkResult.Failure -> {
                        val error = result.error
                        assertInstanceOf<NetworkError.Http>(error)
                        assertEquals(404, error.statusCode)
                        println("Failed to create item. Status code: ${error.statusCode}, message: ${error.message}")
                    }
                }
            }
        }

        @Test
        fun `request can be performed using generated client`() = runBlocking {
            val capturedCatalogId = slot<String?>()
            val capturedQuery = slot<String?>()
            val capturedPage = slot<String?>()
            val capturedSort = slot<String?>()
            val capturedXTracingID = slot<String?>()
            val capturedListParam = slot<List<String>?>()

            testApplication {
                routing {
                    get("/catalogs/{catalogId}/search") {
                        val catalogId = call.parameters["catalogId"]
                        val query = call.request.queryParameters["query"]
                        val page = call.request.queryParameters["page"]
                        val sort = call.request.queryParameters["sort"]
                        val xTracingID = call.request.headers["X-Tracing-ID"]
                        val listParam = call.request.queryParameters.getAll("listParam")

                        capturedCatalogId.captured = catalogId
                        capturedQuery.captured = query
                        capturedPage.captured = page
                        capturedSort.captured = sort
                        capturedXTracingID.captured = xTracingID
                        capturedListParam.captured = listParam

                        call.response.headers.append("Content-Type", "application/json")
                        call.respond("""
                        [
                            {
                                "id": "id-1",
                                "name": "item-a",
                                "description": "description-a",
                                "price": 123.45
                            }
                        ]
                    """.trimIndent())
                    }
                }

                val httpClient = createClient {
                    install(ContentNegotiation) {
                        json()
                    }
                }

                val client = CatalogsSearchClient(httpClient)

                val response = client.searchCatalogItems(
                    catalogId = "catalog-a",
                    query = "query",
                    page = 10,
                    sort = SortOrder.DESC,
                    xTracingID = "request-id-123",
                    listParam = listOf("a", "b", "c")
                )

                assertInstanceOf<NetworkResult.Success<*>>(response)
                assertEquals("catalog-a", capturedCatalogId.captured)
                assertEquals("query", capturedQuery.captured)
                assertEquals("10", capturedPage.captured)
                assertEquals("desc", capturedSort.captured)
                assertEquals("request-id-123", capturedXTracingID.captured)
                assertEquals(listOf("a", "b", "c"), capturedListParam.captured)
            }
        }
    }

    @Nested
    inner class Result {
        @Test
        fun `returns Success on 200 OK`() {
            wiremock.post {
                urlPath like "/catalogs/catalog-a/items"
            } returns {
                statusCode = 200
                body = """
                    {
                        "id": "id-1",
                        "name": "item-a",
                        "description": "description-a",
                        "price": 123.45
                    }
                """.trimIndent()
                header = "Content-Type" to "application/json"
            }

            val client = CatalogsItemsClient(createHttpClient())

            runBlocking {
                val result = client.createItem(
                    item = Item(
                        id = "id-1",
                        name = "item-a",
                        description = "description-a",
                        price = 123.45
                    ),
                    catalogId = "catalog-a",
                    randomNumber = 123,
                    xRequestID = "request-id"
                )

                assertInstanceOf<NetworkResult.Success<*>>(result)

                assertEquals(Item(
                    id = "id-1",
                    name = "item-a",
                    description = "description-a",
                    price = 123.45
                ), (result as NetworkResult.Success).data)
            }
        }

        @Test
        fun `returns Success on 201 Created`() {
            wiremock.post {
                urlPath like "/catalogs/catalog-a/items"
            } returns {
                statusCode = 201
                body = """
                    {
                        "id": "id-1",
                        "name": "item-a",
                        "description": "description-a",
                        "price": 123.45
                    }
                """.trimIndent()
                header = "Content-Type" to "application/json"
            }

            val client = CatalogsItemsClient(createHttpClient())

            runBlocking {
                val result = client.createItem(
                    item = Item(
                        id = "id-1",
                        name = "item-a",
                        description = "description-a",
                        price = 123.45
                    ),
                    catalogId = "catalog-a",
                    randomNumber = 123,
                    xRequestID = "request-id"
                )

                assertInstanceOf<NetworkResult.Success<*>>(result)
            }
        }

        @Test
        fun `returns Error with Http on 302 Found`() {
            wiremock.post {
                urlPath like "/catalogs/catalog-a/items"
            } returns {
                statusCode = 302
                header = "Content-Type" to "application/json"
                header = "Location" to "http://example.com/other-resource"
            }

            val client = CatalogsItemsClient(createHttpClient())

            runBlocking {
                val result = client.createItem(
                    item = Item(
                        id = "id-1",
                        name = "item-a",
                        description = "description-a",
                        price = 123.45
                    ),
                    catalogId = "catalog-a",
                    randomNumber = 123,
                    xRequestID = "request-id"
                )

                assertInstanceOf<NetworkResult.Failure>(result)
                val error = result.error
                assertInstanceOf<NetworkError.Http>(error)
                assertEquals(302, error.statusCode)
            }
        }

        @Test
        fun `returns Success on 204 No Content`() {
            wiremock.get {
                urlPath like "/no-content"
            } returns {
                statusCode = 204
                header = "Content-Type" to "application/json"
            }

            val client = NoContentClient(createHttpClient())

            runBlocking {
                val result = client.getNoContent()

                assertInstanceOf<NetworkResult.Success<*>>(result)
                assertEquals(Unit, (result as NetworkResult.Success).data)
            }
        }
    }

    @Test
    fun `wrong content type returns NetworkError_Unknown`() {
        wiremock.get {
            urlPath like "/catalogs/catalog-a/search"
        } returns {
            statusCode = 200
            body = "[]"
            header = "Content-Type" to "text/plain"
        }

        runBlocking {
            val result = CatalogsSearchClient(createHttpClient())
                .searchCatalogItems(catalogId = "catalog-a", query = "query")

            // With the new error handling, content type issues result in Unknown error
            assertInstanceOf<NetworkResult.Failure>(result)
        }
    }

    @Test
    fun `empty response body returns NetworkError`() {
        wiremock.get {
            urlPath like "/catalogs/catalog-a/search"
        } returns {
            statusCode = 201
            body = "" // No body
            header = "Content-Type" to "application/json"
        }

        runBlocking {
            val result = CatalogsSearchClient(createHttpClient())
                .searchCatalogItems(catalogId = "catalog-a", query = "query")

            // With the new error handling, empty body issues are caught as errors
            assertInstanceOf<NetworkResult.Failure>(result)
            assertInstanceOf<NetworkError.Serialization>(result.error)
        }
    }

    @Test
    fun `invalid response body returns NetworkError_Serialization`() {
        wiremock.get {
            urlPath like "/catalogs/catalog-a/search"
        } returns {
            statusCode = 201
            body = "{}" // should be a list, but is an object
            header = "Content-Type" to "application/json"
        }

        runBlocking {
            val result = CatalogsSearchClient(createHttpClient())
                .searchCatalogItems(catalogId = "catalog-a", query = "query")

            // ContentConvertException is now caught and mapped to NetworkError.Serialization
            assertInstanceOf<NetworkResult.Failure>(result)
            assertInstanceOf<NetworkError.Serialization>(result.error)
        }
    }
}
