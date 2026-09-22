package examples.deprecatedModels.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import kotlin.String

public data class ActiveSubject(
  @param:JsonProperty("id")
  @get:JsonProperty("id")
  public val id: String? = null,
) : LegacyChoice
