package examples.leadingUnderscoreProperty.models

import com.fasterxml.jackson.`annotation`.JsonProperty
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import kotlin.String

public data class ExampleModel(
  @param:JsonProperty("foo")
  @get:JsonProperty("foo")
  @get:NotNull
  public val foo: String,
  @param:JsonProperty("_foo")
  @get:JsonProperty("_foo")
  @get:NotNull
  @get:Valid
  public val _foo: MyCustomType,
)
