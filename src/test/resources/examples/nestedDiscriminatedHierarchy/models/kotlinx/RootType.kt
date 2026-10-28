package examples.nestedDiscriminatedHierarchy.models

import kotlin.String
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@JsonClassDiscriminator("kind")
@ExperimentalSerializationApi
@Serializable
public sealed class RootType() {
  public abstract val rootField: String
}
