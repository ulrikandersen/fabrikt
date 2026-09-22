package examples.discriminatedOneOf.models

import kotlin.Any
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class DiagnosticReport(
  @Contextual
  @SerialName("failure")
  public val failure: Any? = null,
)
