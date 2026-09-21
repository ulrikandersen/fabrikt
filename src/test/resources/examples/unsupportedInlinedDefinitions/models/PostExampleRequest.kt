package examples.unsupportedInlinedDefinitions.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.Int
import kotlin.String

public data class PostExampleRequest(
  @param:JsonProperty("name")
  @get:JsonProperty("name")
  @get:NotNull
  public val name: String,
  @param:JsonProperty("priority")
  @get:JsonProperty("priority")
  public val priority: Int? = null,
)
