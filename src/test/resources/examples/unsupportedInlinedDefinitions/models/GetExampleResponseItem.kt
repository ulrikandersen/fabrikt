package examples.unsupportedInlinedDefinitions.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import kotlin.Int
import kotlin.String

public data class GetExampleResponseItem(
  /**
   * Unique identifier for the item.
   */
  @param:JsonProperty("id")
  @get:JsonProperty("id")
  public val id: Int? = null,
  /**
   * Name of the item.
   */
  @param:JsonProperty("name")
  @get:JsonProperty("name")
  public val name: String? = null,
  /**
   * Status of the item.
   */
  @param:JsonProperty("status")
  @get:JsonProperty("status")
  public val status: GetExampleResponseItemStatus? = null,
)
