package examples.ktorResources.client

import examples.ktorResources.models.SortOrder
import io.ktor.resources.Resource
import kotlin.Double
import kotlin.Int
import kotlin.String

public object Items {
    /**
     * Retrieve a list of items
     *
     * HTTP method: GET
     *
     * Response:
     * 	A successful request returns an HTTP 200 response with
     * [kotlin.collections.List<examples.ktorResources.models.Item>] in the response body.
     *
     * Request parameters:
     * 	 @param limit Maximum number of items to return
     * 	 @param category Filter items by category
     * 	 @param priceLimit Maximum price of items to return
     */
    @Resource("/items")
    public class GetItems(
        public val limit: Int? = null,
        public val category: String? = null,
        public val priceLimit: Double? = null,
    )
}

public object CatalogsItems {
    /**
     * Create a new item
     *
     * HTTP method: POST
     *
     * Request body:
     * 	[examples.ktorResources.models.Item] The item to create
     *
     * Response:
     * 	A successful request returns an HTTP 201 response with [examples.ktorResources.models.Item] in
     * the response body.
     *
     * Request headers:
     * 	"X-Request-ID" (required) Unique identifier for the request
     * 	"X-Tracing-ID" (optional) Unique identifier for the tracing
     *
     * Request parameters:
     * 	 @param catalogId The ID of the catalog
     * 	 @param randomNumber Just a test query param
     */
    @Resource("/catalogs/{catalogId}/items")
    public class CreateItem(
        public val catalogId: String,
        public val randomNumber: Int,
    )
}

public object ItemsSubitems {
    /**
     * Retrieve a specific subitem of an item
     *
     * HTTP method: GET
     *
     * Response:
     * 	A successful request returns an HTTP 200 response with [examples.ktorResources.models.Item] in
     * the response body.
     *
     * Request parameters:
     * 	 @param itemId The ID of the item
     * 	 @param subItemId The ID of the subitem
     */
    @Resource("/items/{itemId}/subitems/{subItemId}")
    public class GetSubItem(
        public val itemId: String,
        public val subItemId: String,
    )
}

public object CatalogsSearch {
    /**
     * Search for items
     *
     * HTTP method: GET
     *
     * Response:
     * 	A successful request returns an HTTP 200 response with
     * [kotlin.collections.List<examples.ktorResources.models.Item>] in the response body.
     *
     * Request parameters:
     * 	 @param catalogId The ID of the catalog
     * 	 @param query The search query
     * 	 @param page Page number
     * 	 @param sort Sort order
     */
    @Resource("/catalogs/{catalogId}/search")
    public class SearchCatalogItems(
        public val catalogId: String,
        public val query: String,
        public val page: Int? = null,
        public val sort: SortOrder? = null,
    )
}

public object CatalogsItemsAvailability {
    /**
     * Check item availability
     *
     * HTTP method: GET
     *
     * Response:
     * 	A successful request returns an HTTP 204 response with an empty body.
     *
     * Request parameters:
     * 	 @param catalogId The ID of the catalog
     * 	 @param itemId The ID of the item
     */
    @Resource("/catalogs/{catalogId}/items/{itemId}/availability")
    public class GetByCatalogIdAndItemId(
        public val catalogId: String,
        public val itemId: String,
    )

    /**
     * Update item availability
     *
     * HTTP method: PUT
     *
     * Response:
     * 	A successful request returns an HTTP 204 response with an empty body.
     *
     * Request parameters:
     * 	 @param catalogId The ID of the catalog
     * 	 @param itemId The ID of the item
     */
    @Resource("/catalogs/{catalogId}/items/{itemId}/availability")
    public class PutByCatalogIdAndItemId(
        public val catalogId: String,
        public val itemId: String,
    )
}

public object Uptime {
    /**
     * Get the uptime of the system
     *
     * HTTP method: GET
     *
     * Response:
     * 	A successful request returns an HTTP 200 response with [kotlin.String] in the response body.
     */
    @Resource("/uptime")
    public class `Get_System-Uptime`()
}
