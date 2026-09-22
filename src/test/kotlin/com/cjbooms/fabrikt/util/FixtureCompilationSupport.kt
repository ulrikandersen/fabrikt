package com.cjbooms.fabrikt.util

import com.cjbooms.fabrikt.cli.ClientCodeGenOptionType
import com.cjbooms.fabrikt.cli.CodeGenTypeOverride
import com.cjbooms.fabrikt.cli.CodeGenerationType
import com.cjbooms.fabrikt.cli.SerializationLibrary
import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.generators.MutableSettings
import com.cjbooms.fabrikt.generators.client.OkHttpClientGenerator
import com.cjbooms.fabrikt.generators.controller.KtorClientGenerator
import com.cjbooms.fabrikt.generators.controller.KtorControllerInterfaceGenerator
import com.cjbooms.fabrikt.generators.model.ModelGenerator
import com.cjbooms.fabrikt.model.SimpleFile
import com.cjbooms.fabrikt.model.SourceApi
import com.cjbooms.fabrikt.model.toFileSpec
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File
import java.nio.file.Path

object FixtureCompilationSupport {
    data class Fixture(
        val id: String,
        val profile: String,
        val scenario: String,
        val sources: List<String>,
    )

    @JvmStatic
    fun main(args: Array<String>) {
        val fixtures = jacksonObjectMapper().readValue<List<Fixture>>(File(args[0]))
        val examples = File(args[1])
        val output = File(args[2])
        output.deleteRecursively()
        fixtures.forEach { fixture ->
            if (fixture.sources.all { "/models/" in it } && fixture.scenario != "validationAnnotations/jakarta") return@forEach
            val sources = fixture.sources.map(examples::resolve)
            val basePackage =
                sources.firstNotNullOf { file ->
                    Regex("(?m)^package (.+)\\.(client|controllers|models)$").find(file.readText())?.groupValues?.get(1)
                }
            MutableSettings.updateSettings(
                genTypes = setOf(CodeGenerationType.HTTP_MODELS),
                serializationLibrary =
                    when (fixture.profile) {
                        "okhttp-jackson3" -> SerializationLibrary.JACKSON_3
                        "ktor-client" -> SerializationLibrary.KOTLINX_SERIALIZATION
                        else -> SerializationLibrary.JACKSON
                    },
                modelSuffix = if (fixture.scenario == "modelSuffix") "Dto" else "",
                typeOverrides =
                    if (fixture.scenario == "byteArrayStream") {
                        setOf(CodeGenTypeOverride.BYTEARRAY_AS_INPUTSTREAM)
                    } else {
                        emptySet()
                    },
            )
            ModelNameRegistry.clear()
            val specScenario = if (fixture.scenario == "validationAnnotations/jakarta") "jakartaValidationAnnotations" else fixture.scenario
            val apiFile = examples.resolve("$specScenario/api.yaml")
            val api = SourceApi(apiFile.readText(), baseUri = apiFile.toURI())
            val packages = Packages(basePackage)
            val destination = output.resolve("${fixture.id}/fixture-support")
            destination.mkdirs()
            val modelSources = sources.filter { it.readText().lineSequence().any { line -> line == "package $basePackage.models" } }
            if (modelSources.isEmpty() || fixture.scenario == "validationAnnotations/jakarta") {
                val declaredTypes =
                    modelSources
                        .flatMap {
                            Regex("\\b(?:class|interface|object) (\\w+)")
                                .findAll(it.readText())
                                .map { match ->
                                    match.groupValues[1]
                                }.toList()
                        }.toSet()
                ModelGenerator(packages, api)
                    .generate()
                    .files
                    .filter { it.name !in declaredTypes }
                    .forEach { it.writeTo(destination) }
            }
            val existingNames = sources.map { it.name }.toSet()
            when (fixture.profile) {
                "okhttp", "okhttp-jackson3" ->
                    OkHttpClientGenerator(packages, api, Path.of(""))
                        .generateLibrary(setOf(ClientCodeGenOptionType.RESILIENCE4J))
                        .filterIsInstance<SimpleFile>()
                        .filter { it.path.fileName.toString() !in existingNames }
                        .forEach { it.writeFileTo(destination) }
                "ktor-client" ->
                    KtorClientGenerator(packages, api, Path.of(""))
                        .generateLibrary(emptySet())
                        .filterIsInstance<SimpleFile>()
                        .filter { it.path.fileName.toString() !in existingNames }
                        .forEach { it.writeFileTo(destination) }
                "ktor-server" ->
                    KtorControllerInterfaceGenerator(packages, api)
                        .generateLibrary()
                        .toFileSpec()
                        .filter { library -> sources.none { Regex("\\bclass ${library.name}\\b").containsMatchIn(it.readText()) } }
                        .forEach { it.writeTo(destination) }
            }
        }
    }
}
