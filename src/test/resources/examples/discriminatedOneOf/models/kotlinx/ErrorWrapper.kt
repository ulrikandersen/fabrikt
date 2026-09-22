package examples.discriminatedOneOf.models

import kotlin.Any
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class ErrorWrapper(
  @Contextual
  @SerialName("error")
  public val error: Any? = null,
)
