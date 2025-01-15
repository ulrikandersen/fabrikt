package examples.ktorResources.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import com.fasterxml.jackson.`annotation`.JsonValue
import javax.validation.constraints.NotNull
import kotlin.Double
import kotlin.String
import kotlin.collections.Map

public data class Item(
    @param:JsonProperty("id")
    @get:JsonProperty("id")
    @get:NotNull
    public val id: String,
    @param:JsonProperty("name")
    @get:JsonProperty("name")
    @get:NotNull
    public val name: String,
    @param:JsonProperty("description")
    @get:JsonProperty("description")
    public val description: String? = null,
    @param:JsonProperty("price")
    @get:JsonProperty("price")
    @get:NotNull
    public val price: Double,
)

public data class NewItem(
    @param:JsonProperty("name")
    @get:JsonProperty("name")
    @get:NotNull
    public val name: String,
    @param:JsonProperty("description")
    @get:JsonProperty("description")
    public val description: String? = null,
    @param:JsonProperty("price")
    @get:JsonProperty("price")
    @get:NotNull
    public val price: Double,
)

public enum class SortOrder(
    @JsonValue
    public val `value`: String,
) {
    ASC("asc"),
    DESC("desc"),
    ;

    public companion object {
        private val mapping: Map<String, SortOrder> = entries.associateBy(SortOrder::value)

        public fun fromValue(`value`: String): SortOrder? = mapping[value]
    }
}

public data class SubItem(
    @param:JsonProperty("id")
    @get:JsonProperty("id")
    public val id: String? = null,
    @param:JsonProperty("parentItemId")
    @get:JsonProperty("parentItemId")
    public val parentItemId: String? = null,
    @param:JsonProperty("name")
    @get:JsonProperty("name")
    public val name: String? = null,
    @param:JsonProperty("description")
    @get:JsonProperty("description")
    public val description: String? = null,
)

public data class UpdateItem(
    @param:JsonProperty("name")
    @get:JsonProperty("name")
    public val name: String? = null,
    @param:JsonProperty("description")
    @get:JsonProperty("description")
    public val description: String? = null,
    @param:JsonProperty("price")
    @get:JsonProperty("price")
    public val price: Double? = null,
)
