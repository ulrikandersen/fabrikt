package examples.discriminatedOneOf.models

import jakarta.validation.constraints.NotNull
import kotlin.Any
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class PromotionEffect(
  @Contextual
  @SerialName("additionalData")
  @get:NotNull
  public val additionalData: Any,
)
