package examples.unsupportedInlinedDefinitions.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import kotlin.String

public data class InlineObj(
  @param:JsonProperty("test")
  @get:JsonProperty("test")
  public val test: String? = null,
)
