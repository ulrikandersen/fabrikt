package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import javax.validation.constraints.NotNull
import kotlin.String

public data class ClickEvent(
  @get:JsonProperty("eventType")
  @get:NotNull
  @param:JsonProperty("eventType")
  public val eventType: String = "click",
) : Event
