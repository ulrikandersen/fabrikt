package examples.inlineResponseObject.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.String

public data class WidgetResponse(
  @param:JsonProperty("id")
  @get:JsonProperty("id")
  @get:NotNull
  public val id: String,
  @param:JsonProperty("name")
  @get:JsonProperty("name")
  @get:NotNull
  public val name: String,
)
