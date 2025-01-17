package com.cjbooms.fabrikt.routing.ktor

import com.example.client.CatalogsItemsAvailability
import com.example.client.CatalogsItemsAvailabilityGetByCatalogIdAndItemId
import com.example.client.CatalogsSearch
import com.example.client.`Get_System-Uptime`
import com.example.client.SearchCatalogItems
import com.example.client.Uptime
import com.example.models.SortOrder
import io.ktor.client.plugins.resources.Resources
import io.ktor.client.plugins.resources.get
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import io.ktor.resources.href
import io.ktor.resources.serialization.ResourcesFormat
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.testing.testApplication
import io.mockk.slot
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import javax.xml.catalog.Catalog
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KtorClientRoutingTest {

    @Test
    fun `should generate URI with required params`() {
        val resFormat = ResourcesFormat()

        val uri = href(resFormat, CatalogsSearch.SearchCatalogItems("catalog-a", "query"))

        assertEquals("/catalogs/catalog-a/search?query=query", uri)
    }

    @Test
    fun `should generate URI with all params`() {
        val resFormat = ResourcesFormat()

        val uri = href(
            resFormat,
            CatalogsSearch.SearchCatalogItems(
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
    fun `request can be performed using Ktor client and the Resources plugin`() = runBlocking {
        val capturedCatalogId = slot<String?>()
        val capturedQuery = slot<String?>()
        val capturedPage = slot<String?>()
        val capturedSort = slot<String?>()

        testApplication {
            routing {
                get("/catalogs/{catalogId}/search") {
                    val catalogId = call.parameters["catalogId"]
                    val query = call.request.queryParameters["query"]
                    val page = call.request.queryParameters["page"]
                    val sort = call.request.queryParameters["sort"]

                    capturedCatalogId.captured = catalogId
                    capturedQuery.captured = query
                    capturedPage.captured = page
                    capturedSort.captured = sort

                    call.respond(HttpStatusCode.NoContent)
                }
            }

            val client = createClient {
                install(Resources)
            }

            val httpResponse = client.get(
                CatalogsSearch.SearchCatalogItems(
                    catalogId = "catalog-a",
                    query = "query",
                    page = 10,
                    sort = SortOrder.DESC
                )
            )

            assertTrue(httpResponse.status.isSuccess())
            assertEquals("catalog-a", capturedCatalogId.captured)
            assertEquals("query", capturedQuery.captured)
            assertEquals("10", capturedPage.captured)
            assertEquals("desc", capturedSort.captured)
        }
    }

    @Test
    fun `resource name is generated correctly`() {
        val resFormat = ResourcesFormat()

        val uri = href(resFormat, CatalogsItemsAvailability.GetByCatalogIdAndItemId("catalog-a", "item-b"))

        assertEquals("/catalogs/catalog-a/items/item-b/availability", uri)
    }

    @Test
    fun `operationId with underscore and dash works`() {
        val resFormat = ResourcesFormat()

        val uri = href(resFormat, Uptime.`Get_System-Uptime`())

        assertEquals("/uptime", uri)
    }
}
