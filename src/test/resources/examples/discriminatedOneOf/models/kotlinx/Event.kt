package examples.discriminatedOneOf.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@Serializable
@JsonClassDiscriminator("eventType")
public sealed interface Event
