package examples.discriminatedOneOf.models

import com.fasterxml.jackson.`annotation`.JsonSubTypes
import com.fasterxml.jackson.`annotation`.JsonTypeInfo

@JsonTypeInfo(
  use = JsonTypeInfo.Id.NAME,
  include = JsonTypeInfo.As.EXISTING_PROPERTY,
  property = "eventType",
  visible = true,
)
@JsonSubTypes(JsonSubTypes.Type(value = ClickEvent::class, name = "click"),JsonSubTypes.Type(value =
    ScrollEvent::class, name = "scroll"))
public sealed interface Event
