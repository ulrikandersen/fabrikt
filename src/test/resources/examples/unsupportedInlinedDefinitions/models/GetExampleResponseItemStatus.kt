package examples.unsupportedInlinedDefinitions.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.String
import kotlin.collections.Map

/**
 * Status of the item.
 */
public enum class GetExampleResponseItemStatus(
  @JsonValue
  public val `value`: String,
) {
  ACTIVE("active"),
  INACTIVE("inactive"),
  ARCHIVED("archived"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, GetExampleResponseItemStatus> =
        entries.associateBy(GetExampleResponseItemStatus::value)

    public fun fromValue(`value`: String): GetExampleResponseItemStatus? = mapping[value]
  }
}
