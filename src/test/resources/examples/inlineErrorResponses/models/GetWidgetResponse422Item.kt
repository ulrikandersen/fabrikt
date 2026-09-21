package examples.inlineErrorResponses.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.String

public data class GetWidgetResponse422Item(
  @param:JsonProperty("reason")
  @get:JsonProperty("reason")
  @get:NotNull
  public val reason: String,
)
