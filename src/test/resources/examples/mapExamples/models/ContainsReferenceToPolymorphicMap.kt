package examples.mapExamples.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import kotlin.Any
import kotlin.String
import kotlin.collections.Map

public data class ContainsReferenceToPolymorphicMap(
  @param:JsonProperty("attributes")
  @get:JsonProperty("attributes")
  public val attributes: Map<String, Any?>?,
)
