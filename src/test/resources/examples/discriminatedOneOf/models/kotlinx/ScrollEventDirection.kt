package examples.discriminatedOneOf.models

import kotlin.String
import kotlin.collections.Map
import kotlinx.serialization.SerialName

public enum class ScrollEventDirection(
  public val `value`: String,
) {
  @SerialName("x")
  X("x"),
  @SerialName("y")
  Y("y"),
  ;

  public companion object {
    private val mapping: Map<String, ScrollEventDirection> =
        values().associateBy(ScrollEventDirection::value)

    public fun fromValue(`value`: String): ScrollEventDirection? = mapping[value]
  }
}
