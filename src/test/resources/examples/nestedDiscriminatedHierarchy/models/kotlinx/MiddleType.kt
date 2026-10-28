package examples.nestedDiscriminatedHierarchy.models

import kotlin.String
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@SerialName("middle")
@JsonClassDiscriminator("kind")
@ExperimentalSerializationApi
@Serializable
public sealed class MiddleType() : RootType() {
  public abstract val middleField: String?
}
