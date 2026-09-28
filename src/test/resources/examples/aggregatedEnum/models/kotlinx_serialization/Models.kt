package examples.aggregatedEnum.models

import jakarta.validation.constraints.NotNull
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.String
import kotlin.collections.List
import kotlin.collections.Map

public enum class SomeEnumDto(
    public val `value`: String,
) {
    @SerialName("foo")
    FOO("foo"),

    @SerialName("bar")
    BAR("bar"),

    @SerialName("baz")
    BAZ("baz"),
    ;

    override fun toString(): String = value

    public companion object {
        private val mapping: Map<String, SomeEnumDto> = entries.associateBy(SomeEnumDto::value)

        public fun fromValue(`value`: String): SomeEnumDto? = mapping[value]
    }
}

@Serializable
public data class SomeObjectDto(
    @SerialName("direct")
    public val direct: SomeEnumDto? = null,
    /**
     * A wrapped enum with its own default.
     */
    @SerialName("withDefault")
    @get:NotNull
    public val withDefault: SomeEnumDto = SomeEnumDto.BAZ,
    @SerialName("withoutDefault")
    public val withoutDefault: SomeEnumDto? = null,
    @SerialName("nullable")
    public val nullable: SomeEnumDto? = SomeEnumDto.BAZ,
    @SerialName("anyOfDefault")
    @get:NotNull
    public val anyOfDefault: SomeEnumDto = SomeEnumDto.BAR,
    @SerialName("values")
    public val values: List<SomeEnumDto>? = null,
)
