package examples.jvmGetterNameCollisions.models

import kotlin.Boolean
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class GetterNames(
  @SerialName("x")
  public val x: String? = null,
  @SerialName("X")
  public val X___: String? = null,
  @SerialName("X_")
  public val x_: String? = null,
  @SerialName("x__")
  public val x__: String? = null,
  @SerialName("isReady")
  public val isReady: Boolean? = null,
  @SerialName("IsReady")
  public val IsReady: Boolean? = null,
  @SerialName("other")
  public val other: String? = null,
)
