package com.cjbooms.fabrikt.clients

import com.example.client.CatalogsItemsClient
import com.example.client.CatalogsSearchClient
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
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.ContentConvertException
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
import org.junit.jupiter.api.assertThrows
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
                    is CatalogsItemsClient.CreateItemResult.Success -> {
                        println("Created item with name: ${result.data.name}. Status code: ${result.response.status}")
                    }

                    is CatalogsItemsClient.CreateItemResult.Failure -> {
                        fail("Failed to create item.\nStatus code: ${result.response.status}\nBody: ${result.response.bodyAsText()}")
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
                    is CatalogsItemsClient.CreateItemResult.Success -> {
                        fail("Expected 404 but got success")
                    }

                    is CatalogsItemsClient.CreateItemResult.Failure -> {
                        println("Failed to create item. Status code: ${result.response.status}")
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

                assertInstanceOf<CatalogsSearchClient.SearchCatalogItemsResult.Success>(response)
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

                assertInstanceOf<CatalogsItemsClient.CreateItemResult.Success>(result)

                assertEquals(Item(
                    id = "id-1",
                    name = "item-a",
                    description = "description-a",
                    price = 123.45
                ), result.data)
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

                assertInstanceOf<CatalogsItemsClient.CreateItemResult.Success>(result)
            }
        }

        @Test
        fun `returns Failure on 302 Found`() {
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

                assertInstanceOf<CatalogsItemsClient.CreateItemResult.Failure>(result)
                assertEquals(302, result.response.status.value)
                assertEquals("http://example.com/other-resource", result.response.headers["Location"])
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

                assertInstanceOf<NoContentClient.GetNoContentResult.Success>(result)
                assertEquals(Unit, result.data)
            }
        }
    }

    @Test
    fun `wrong content type throws Ktor's NoTransformationFoundException`() {
        wiremock.get {
            urlPath like "/catalogs/catalog-a/search"
        } returns {
            statusCode = 200
            body = "[]"
            header = "Content-Type" to "text/plain"
        }

        runBlocking {
            assertThrows<NoTransformationFoundException> {
                CatalogsSearchClient(createHttpClient())
                    .searchCatalogItems(catalogId = "catalog-a", query = "query")
            }
        }
    }

    @Test
    fun `empty response body throws Ktor's ContentConvertException`() {
        wiremock.get {
            urlPath like "/catalogs/catalog-a/search"
        } returns {
            statusCode = 201
            body = "" // No body
            header = "Content-Type" to "application/json"
        }

        val exception = runBlocking {
            assertThrows<ContentConvertException> {
                CatalogsSearchClient(createHttpClient())
                    .searchCatalogItems(catalogId = "catalog-a", query = "query")
            }
        }

        assertEquals("No suitable converter found for TypeInfo(kotlin.collections.List<com.example.models.Item>)", exception.message)
    }

    @Test
    fun `invalid response body throws Ktor's ContentConvertException`() {
        wiremock.get {
            urlPath like "/catalogs/catalog-a/search"
        } returns {
            statusCode = 201
            body = "{}" // should be a list, but is an object
            header = "Content-Type" to "application/json"
        }

        val exception = runBlocking {
            assertThrows<ContentConvertException> {
                CatalogsSearchClient(createHttpClient())
                    .searchCatalogItems(catalogId = "catalog-a", query = "query")
            }
        }

        assertEquals("Illegal input: Unexpected JSON token at offset 0: Expected start of the array '[', but had '{' instead at path: \$\nJSON input: {}", exception.message)
    }
}
