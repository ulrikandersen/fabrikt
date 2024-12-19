package examples.discriminatedOneOf.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SerialName("scroll")
@Serializable
public object ScrollEvent : Event
