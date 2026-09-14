package com.cjbooms.fabrikt.generators

import com.cjbooms.fabrikt.cli.ClientCodeGenOptionType
import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.generators.client.OkHttpEnhancedClientGenerator
import com.cjbooms.fabrikt.generators.client.OkHttpSimpleClientGenerator
import com.cjbooms.fabrikt.generators.client.OpenFeignInterfaceGenerator
import com.cjbooms.fabrikt.generators.client.SpringHttpInterfaceGenerator
import com.cjbooms.fabrikt.generators.controller.KtorClientGenerator
import com.cjbooms.fabrikt.generators.controller.KtorControllerInterfaceGenerator
import com.cjbooms.fabrikt.generators.controller.MicronautControllerInterfaceGenerator
import com.cjbooms.fabrikt.generators.controller.SpringControllerInterfaceGenerator
import com.cjbooms.fabrikt.model.SourceApi
import com.cjbooms.fabrikt.util.ModelNameRegistry
import com.squareup.kotlinpoet.asClassName
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DeprecatedElementGenerationTest {
    private val packages = Packages("examples.deprecatedOperations")
    private val sourceApi by lazy {
        SourceApi(javaClass.getResource("/examples/deprecatedOperations/api.yaml")!!.readText())
    }

    @BeforeEach
    fun init() {
        MutableSettings.updateSettings()
        ModelNameRegistry.clear()
    }

    @Test
    fun `deprecated operations are annotated for every client and controller target`() {
        val generatedTypes =
            listOf(
                SpringHttpInterfaceGenerator(packages, sourceApi).generate(emptySet()).clients,
                OpenFeignInterfaceGenerator(packages, sourceApi).generate(emptySet()).clients,
                OkHttpSimpleClientGenerator(packages, sourceApi).generateDynamicClientCode(),
                OkHttpEnhancedClientGenerator(packages, sourceApi)
                    .generateDynamicClientCode(setOf(ClientCodeGenOptionType.RESILIENCE4J)),
                KtorClientGenerator(packages, sourceApi).generate(emptySet()).clients,
                SpringControllerInterfaceGenerator(packages, sourceApi, JavaxValidationAnnotations).generate().controllers,
                MicronautControllerInterfaceGenerator(packages, sourceApi, JavaxValidationAnnotations).generate().controllers,
                KtorControllerInterfaceGenerator(packages, sourceApi).generate().controllers,
            )

        generatedTypes.forEach { types ->
            val functions = types.flatMap { it.spec.funSpecs }
            val deprecatedOperation = functions.single { it.name == "findSubject" }
            val activeOperation = functions.single { it.name == "replaceSubject" }

            assertThat(deprecatedOperation.annotations.map { it.typeName })
                .contains(Deprecated::class.asClassName())
            assertThat(activeOperation.annotations.map { it.typeName })
                .doesNotContain(Deprecated::class.asClassName())
        }
    }
}
