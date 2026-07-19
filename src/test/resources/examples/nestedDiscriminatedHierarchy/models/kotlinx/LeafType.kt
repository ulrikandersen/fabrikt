package examples.nestedDiscriminatedHierarchy.models

import jakarta.validation.constraints.NotNull
import kotlin.String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SerialName("leaf")
@Serializable
public data class LeafType(
  @SerialName("rootField")
  @get:NotNull
  override val rootField: String,
  @SerialName("middleField")
  override val middleField: String? = null,
  @SerialName("leafField")
  public val leafField: String? = null,
) : MiddleType()
