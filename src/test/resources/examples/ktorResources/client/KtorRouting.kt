package examples.ktorResources.client

import examples.ktorResources.models.SortOrder
import io.ktor.resources.Resource
import kotlin.Int
import kotlin.String

/**
 * Retrieve a list of items
 *
 * A successful request returns an HTTP 200 response with
 * [kotlin.collections.List<examples.ktorResources.models.Item>] in the response body.
 *
 * @param limit Maximum number of items to return
 * @param category Filter items by category
 */
@Resource("/items")
public class GetItems(
    public val limit: Int? = null,
    public val category: String? = null,
)

/**
 * Create a new item
 *
 * A successful request returns an HTTP 201 response with [examples.ktorResources.models.Item] in
 * the response body.
 *
 * @param newItem
 */
@Resource("/items")
public class CreateItem()

/**
 * Retrieve a specific subitem of an item
 *
 * A successful request returns an HTTP 200 response with [examples.ktorResources.models.SubItem] in
 * the response body.
 *
 * @param itemId The ID of the item
 * @param subItemId The ID of the subitem
 */
@Resource("/items/{itemId}/subitems/{subItemId}")
public class GetSubItem(
    public val itemId: String,
    public val subItemId: String,
)

/**
 * Search for items
 *
 * A successful request returns an HTTP 200 response with
 * [kotlin.collections.List<examples.ktorResources.models.Item>] in the response body.
 *
 * @param catalogId The ID of the catalog
 * @param query The search query
 * @param page Page number
 * @param sort Sort order
 */
@Resource("/catalogs/{catalogId}/search")
public class SearchCatalogItems(
    public val catalogId: String,
    public val query: String,
    public val page: Int? = null,
    public val sort: SortOrder? = null,
)

/**
 * Check the health of the system
 *
 * A successful request returns an HTTP 204 response with an empty body.
 */
@Resource("/health")
public class HealthGet()

/**
 * Get the uptime of the system
 *
 * A successful request returns an HTTP 200 response with [kotlin.String] in the response body.
 */
@Resource("/uptime")
public class `Get_System-Uptime`()
