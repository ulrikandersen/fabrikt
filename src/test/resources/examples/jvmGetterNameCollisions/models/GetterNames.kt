package examples.jvmGetterNameCollisions.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import kotlin.Boolean
import kotlin.String

public data class GetterNames(
  @param:JsonProperty("x")
  @get:JsonProperty("x")
  public val x: String? = null,
  @param:JsonProperty("X")
  @get:JsonProperty("X")
  public val X___: String? = null,
  @param:JsonProperty("X_")
  @get:JsonProperty("X_")
  public val x_: String? = null,
  @param:JsonProperty("x__")
  @get:JsonProperty("x__")
  public val x__: String? = null,
  @param:JsonProperty("isReady")
  @get:JsonProperty("isReady")
  public val isReady: Boolean? = null,
  @param:JsonProperty("IsReady")
  @get:JsonProperty("IsReady")
  public val IsReady: Boolean? = null,
  @param:JsonProperty("other")
  @get:JsonProperty("other")
  public val other: String? = null,
)
