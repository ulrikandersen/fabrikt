package com.cjbooms.fabrikt.routing.ktor

import com.example.client.`Get_System-Uptime`
import com.example.client.ParameterNameClash
import com.example.client.SearchCatalogItems
import com.example.models.Item
import com.example.models.SortOrder
import io.ktor.client.call.body
import io.ktor.client.plugins.resources.Resources
import io.ktor.client.plugins.resources.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import io.ktor.resources.Resource
import io.ktor.resources.href
import io.ktor.resources.serialization.ResourcesFormat
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.testing.testApplication
import io.mockk.slot
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KtorClientRoutingTest {

    @Test
    fun `should generate URI with required params`() {
        val resFormat = ResourcesFormat()

        val uri = href(resFormat, SearchCatalogItems("catalog-a", "query"))

        assertEquals("/catalogs/catalog-a/search?query=query", uri)
    }

    @Test
    fun `should generate URI with all params`() {
        val resFormat = ResourcesFormat()

        val uri = href(
            resFormat,
            SearchCatalogItems(
                catalogId = "catalog-a",
                query = "query",
                page = 1,
                sort = SortOrder.ASC
            )
        )

        assertEquals(
            "/catalogs/catalog-a/search?query=query&page=1&sort=asc",
            uri
        )
    }

    @Test
    fun `should perform a request`() = runBlocking {
        val capturedCatalogId = slot<String?>()
        val capturedQuery = slot<String?>()
        val capturedPage = slot<String?>()
        val capturedSort = slot<String?>()

        testApplication {
            routing {
                install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
                    json(Json {
                        prettyPrint = true
                    })
                }

                get("/catalogs/{catalogId}/search") {
                    val catalogId = call.parameters["catalogId"]
                    val query = call.request.queryParameters["query"]
                    val page = call.request.queryParameters["page"]
                    val sort = call.request.queryParameters["sort"]

                    capturedCatalogId.captured = catalogId
                    capturedQuery.captured = query
                    capturedPage.captured = page
                    capturedSort.captured = sort

                    call.respond(listOf(Item(
                        id = "someId",
                        name = "name",
                        price = 22.222,
                    )))
                }
            }

            val client = createClient {
                install(Resources)
                install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                    json()
                }
            }

            val httpResponse = client.get(
                SearchCatalogItems(
                    catalogId = "catalog-a",
                    query = "query",
                    page = 10,
                    sort = SortOrder.DESC
                )
            )

            assertTrue(httpResponse.status.isSuccess())

            assertEquals("""
                [
                    {
                        "id": "someId",
                        "name": "name",
                        "price": 22.222
                    }
                ]
            """.trimIndent(), httpResponse.bodyAsText())

            assertEquals(
                listOf(
                    Item("someId", "name", price = 22.222)
            ), httpResponse.body())

            assertEquals("catalog-a", capturedCatalogId.captured)
            assertEquals("query", capturedQuery.captured)
            assertEquals("10", capturedPage.captured)
            assertEquals("desc", capturedSort.captured)
        }
    }

    @Test
    fun `operationId with special characters`() {
        val resFormat = ResourcesFormat()

        val uri = href(resFormat, `Get_System-Uptime`())

        assertEquals("/uptime", uri)
    }
}
