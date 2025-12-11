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
import io.ktor.client.plugins.contentnegotiation.ContentNegotiationConfig
import io.ktor.client.plugins.defaultRequest
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.testing.ApplicationTestBuilder
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
abstract class KtorClientTestBase {

    private val port: Int = ServerSocket(0).use { socket -> socket.localPort }
    protected val wiremock: WireMockServer = WireMockServer(options().port(port).notifier(ConsoleNotifier(true)))

    init {
        wiremock.start()
    }

    @BeforeEach
    fun setUp() {
        wiremock.resetAll()
    }

    abstract fun ContentNegotiationConfig.configureSerializer()

    protected fun createHttpClient(): HttpClient = HttpClient(CIO) {
        install(ContentNegotiation) { configureSerializer() }
        defaultRequest { url(wiremock.baseUrl()) }
    }

    protected fun ApplicationTestBuilder.createTestClient(): HttpClient = createClient {
        install(ContentNegotiation) { configureSerializer() }
    }

    @Nested
    inner class Client {
        @Test
        fun `client performs post with body`() {
            wiremock.post {
                urlPath like "/catalogs/catalog-a/items"
            } returns {
                statusCode = 201
                body = """{"id": "id-1", "name": "item-a", "description": "description-a", "price": 123.45}"""
                header = "Content-Type" to "application/json"
            }

            runBlocking {
                val result = CatalogsItemsClient(createHttpClient()).createItem(
                    item = Item(id = "id-1", name = "item-a", description = "description-a", price = 123.45),
                    catalogId = "catalog-a",
                    randomNumber = 123,
                    xRequestID = "request-id"
                )

                when (result) {
                    is NetworkResult.Success -> println("Created item with name: ${result.data.name}")
                    is NetworkResult.Failure -> fail("Failed to create item: ${result.error}")
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

            runBlocking {
                val result = CatalogsItemsClient(createHttpClient()).createItem(
                    item = Item(id = "id-1", name = "item-a", description = "description-a", price = 123.45),
                    catalogId = "catalog-a",
                    randomNumber = 123,
                    xRequestID = "request-id"
                )

                when (result) {
                    is NetworkResult.Success -> fail("Expected 404 but got success")
                    is NetworkResult.Failure -> {
                        assertInstanceOf<NetworkError.Http>(result.error)
                        assertEquals(404, (result.error as NetworkError.Http).statusCode)
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
                        capturedCatalogId.captured = call.parameters["catalogId"]
                        capturedQuery.captured = call.request.queryParameters["query"]
                        capturedPage.captured = call.request.queryParameters["page"]
                        capturedSort.captured = call.request.queryParameters["sort"]
                        capturedXTracingID.captured = call.request.headers["X-Tracing-ID"]
                        capturedListParam.captured = call.request.queryParameters.getAll("listParam")

                        call.response.headers.append("Content-Type", "application/json")
                        call.respond("""[{"id": "id-1", "name": "item-a", "description": "description-a", "price": 123.45}]""")
                    }
                }

                val response = CatalogsSearchClient(createTestClient()).searchCatalogItems(
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
                body = """{"id": "id-1", "name": "item-a", "description": "description-a", "price": 123.45}"""
                header = "Content-Type" to "application/json"
            }

            runBlocking {
                val result = CatalogsItemsClient(createHttpClient()).createItem(
                    item = Item(id = "id-1", name = "item-a", description = "description-a", price = 123.45),
                    catalogId = "catalog-a",
                    randomNumber = 123,
                    xRequestID = "request-id"
                )

                assertInstanceOf<NetworkResult.Success<*>>(result)
                assertEquals(
                    Item(id = "id-1", name = "item-a", description = "description-a", price = 123.45),
                    (result as NetworkResult.Success).data
                )
            }
        }

        @Test
        fun `returns Success on 201 Created`() {
            wiremock.post {
                urlPath like "/catalogs/catalog-a/items"
            } returns {
                statusCode = 201
                body = """{"id": "id-1", "name": "item-a", "description": "description-a", "price": 123.45}"""
                header = "Content-Type" to "application/json"
            }

            runBlocking {
                val result = CatalogsItemsClient(createHttpClient()).createItem(
                    item = Item(id = "id-1", name = "item-a", description = "description-a", price = 123.45),
                    catalogId = "catalog-a",
                    randomNumber = 123,
                    xRequestID = "request-id"
                )
                assertInstanceOf<NetworkResult.Success<*>>(result)
            }
        }

        @Test
        fun `returns Failure with Http on 302 Found`() {
            wiremock.post {
                urlPath like "/catalogs/catalog-a/items"
            } returns {
                statusCode = 302
                header = "Content-Type" to "application/json"
                header = "Location" to "http://example.com/other-resource"
            }

            runBlocking {
                val result = CatalogsItemsClient(createHttpClient()).createItem(
                    item = Item(id = "id-1", name = "item-a", description = "description-a", price = 123.45),
                    catalogId = "catalog-a",
                    randomNumber = 123,
                    xRequestID = "request-id"
                )

                assertInstanceOf<NetworkResult.Failure>(result)
                assertInstanceOf<NetworkError.Http>((result as NetworkResult.Failure).error)
                assertEquals(302, (result.error as NetworkError.Http).statusCode)
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

            runBlocking {
                val result = NoContentClient(createHttpClient()).getNoContent()
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

            assertInstanceOf<NetworkResult.Failure>(result)
            assertInstanceOf<NetworkError.Unknown>((result as NetworkResult.Failure).error)
        }
    }

    @Test
    fun `empty response body returns NetworkError_Serialization`() {
        wiremock.get {
            urlPath like "/catalogs/catalog-a/search"
        } returns {
            statusCode = 201
            body = ""
            header = "Content-Type" to "application/json"
        }

        val result = runBlocking {
            CatalogsSearchClient(createHttpClient())
                .searchCatalogItems(catalogId = "catalog-a", query = "query")
        }

        assertInstanceOf<NetworkResult.Failure>(result)
        assertInstanceOf<NetworkError.Serialization>((result as NetworkResult.Failure).error)
    }

    @Test
    fun `invalid response body returns NetworkError_Serialization`() {
        wiremock.get {
            urlPath like "/catalogs/catalog-a/search"
        } returns {
            statusCode = 201
            body = "{}"
            header = "Content-Type" to "application/json"
        }

        val result = runBlocking {
            CatalogsSearchClient(createHttpClient())
                .searchCatalogItems(catalogId = "catalog-a", query = "query")
        }

        assertInstanceOf<NetworkResult.Failure>(result)
        assertInstanceOf<NetworkError.Serialization>((result as NetworkResult.Failure).error)
    }
}
