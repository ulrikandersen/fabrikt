package com.cjbooms.fabrikt.models.kotlinx

import com.example.models.LeafType
import com.example.models.MiddleType
import com.example.models.RootType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Covers a three-level allOf hierarchy where the middle type is both a polymorphic subtype and
 * a polymorphic supertype. The middle type previously received a duplicate @Serializable
 * annotation, which does not compile.
 */
class KotlinxSerializationNestedHierarchyTest {

    @Test
    fun `must round-trip a leaf through the middle of the hierarchy`() {
        val json = """{"kind":"leaf","rootField":"root","middleField":"middle","leafField":"leaf"}"""
        val middle: MiddleType = Json.decodeFromString(json)

        assertThat(middle).isEqualTo(LeafType(rootField = "root", middleField = "middle", leafField = "leaf"))
        assertThat(Json.encodeToString(middle)).isEqualTo(json)
    }

    @Test
    fun `must round-trip a leaf through the root of the hierarchy`() {
        val json = """{"kind":"leaf","rootField":"root","middleField":"middle","leafField":"leaf"}"""
        val root: RootType = Json.decodeFromString(json)

        assertThat(root).isEqualTo(LeafType(rootField = "root", middleField = "middle", leafField = "leaf"))
        assertThat(Json.encodeToString(root)).isEqualTo(json)
    }
}
