package examples.deprecatedModels.models

import com.fasterxml.jackson.`annotation`.JsonValue
import kotlin.Deprecated
import kotlin.String
import kotlin.collections.Map

@Deprecated(message = "This API schema is deprecated.")
public enum class LegacyStatus(
  @JsonValue
  public val `value`: String,
) {
  ACTIVE("active"),
  RETIRED("retired"),
  ;

  override fun toString(): String = value

  public companion object {
    private val mapping: Map<String, LegacyStatus> = entries.associateBy(LegacyStatus::value)

    public fun fromValue(`value`: String): LegacyStatus? = mapping[value]
  }
}
