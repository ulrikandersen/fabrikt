package examples.deprecatedModels.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.constraints.NotNull
import kotlin.Deprecated
import kotlin.String

@Deprecated(message = "This API schema is deprecated.")
public data class LegacySubject(
  @Deprecated(message = "This API property is deprecated.")
  @param:JsonProperty("legacyId")
  @get:JsonProperty("legacyId")
  @get:NotNull
  public val legacyId: String,
) : LegacyChoice
