package com.cjbooms.fabrikt.generators.controller

import com.cjbooms.fabrikt.cli.ClientCodeGenOptionType
import com.cjbooms.fabrikt.configurations.Packages
import com.cjbooms.fabrikt.generators.GeneratorUtils.hasNameClashes
import com.cjbooms.fabrikt.generators.GeneratorUtils.isUnit
import com.cjbooms.fabrikt.generators.GeneratorUtils.splitByType
import com.cjbooms.fabrikt.generators.GeneratorUtils.toIncomingParameters
import com.cjbooms.fabrikt.generators.GeneratorUtils.toKCodeName
import com.cjbooms.fabrikt.generators.client.ClientGenerator
import com.cjbooms.fabrikt.generators.controller.ControllerGeneratorUtils.happyPathResponse
import com.cjbooms.fabrikt.model.ClientType
import com.cjbooms.fabrikt.model.Clients
import com.cjbooms.fabrikt.model.GeneratedFile
import com.cjbooms.fabrikt.model.IncomingParameter
import com.cjbooms.fabrikt.model.RequestParameter
import com.cjbooms.fabrikt.model.SourceApi
import com.cjbooms.fabrikt.util.KaizenParserExtensions.routeToPaths
import com.reprezen.kaizen.oasparser.model3.Operation
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

class KtorRoutingResourcesGenerator(
    private val packages: Packages,
    private val api: SourceApi,
) : ClientGenerator {
    override fun generate(options: Set<ClientCodeGenOptionType>): Clients {
        val resources: List<TypeSpec> = api.openApi3.routeToPaths().flatMap { (resourceName, paths) ->
            paths.flatMap { path ->
                path.value.operations.map { (verb, operation) ->
                    val params = operation.toIncomingParameters(packages.base, path.value.parameters, emptyList(),
                        avoidNameClashes = false
                    )
                    require(!params.hasNameClashes()) { """
                        Operation '${path.value.pathString} > ${verb}' has name clashes in parameters. Ktor type safe 
                        routing requires parameter names to be unique.
                    """.trimIndent().replace("\n", "") }

                    val (pathParams, queryParams, _, _) = params.splitByType()

                    val resourceClassBuilder = TypeSpec.classBuilder(resourceClassName(operation, resourceName, verb, pathParams))
                        .addAnnotation(AnnotationSpec.builder(ClassName("io.ktor.resources", "Resource"))
                            .addMember("%S", path.value.pathString).build())

                    val constructorBuilder = FunSpec.constructorBuilder()

                    // Add path parameters to constructor
                    pathParams.forEach { param ->
                        constructorBuilder.addParameter(
                            ParameterSpec.builder(param.name, param.type.copy(nullable = !param.isRequired))
                                .build()
                        )
                        resourceClassBuilder.addProperty(
                            PropertySpec.builder(param.name, param.type.copy(nullable = !param.isRequired))
                                .initializer(param.name)
                                .build()
                        )
                    }

                    // Add query parameters to constructor
                    queryParams.forEach { param ->
                        val defaultValue = if (!param.isRequired) "null" else null
                        constructorBuilder.addParameter(
                            ParameterSpec.builder(param.name, param.type.copy(nullable = !param.isRequired))
                                .apply { if (defaultValue != null) defaultValue(defaultValue) }
                                .build()
                        )
                        resourceClassBuilder.addProperty(
                            PropertySpec.builder(param.name, param.type.copy(nullable = !param.isRequired))
                                .initializer(param.name)
                                .build()
                        )
                    }

                    resourceClassBuilder.primaryConstructor(constructorBuilder.build())

                    resourceClassBuilder.addKdoc(buildResourceKdoc(operation, params, verb))

                    resourceClassBuilder.build()
                }
            }
        }

        return Clients(resources.map { ClientType(it, packages.base) }.toSet())
    }

    override fun generateLibrary(options: Set<ClientCodeGenOptionType>): Collection<GeneratedFile> {
        return emptyList()
    }

    private fun resourceClassName(op: Operation, resourceName: String, verb: String, params: List<RequestParameter>) =
        if (op.operationId != null) {
            op.operationId.replaceFirstChar { it.uppercase() }
        } else {
            buildString {
                append(resourceName)
                append(verb.replaceFirstChar { it.uppercase() })
                append(if (params.isNotEmpty()) "By" + params.joinToString("And") { it -> it.name.replaceFirstChar { it.uppercase() } } else "")
            }
        }

    private fun buildResourceKdoc(operation: Operation, parameters: List<IncomingParameter>, verb: String): CodeBlock {
        val (pathParams, queryParams, headerParams, bodyParams) = parameters.splitByType()
        val kDoc = CodeBlock.builder()

        // add summary and description
        val methodDesc = listOf(operation.summary.orEmpty(), operation.description.orEmpty()).filter { it.isNotEmpty() }
        if (methodDesc.isNotEmpty()) {
            methodDesc.forEach { kDoc.add("%L\n", it) }
            kDoc.add("\n")
        }

        // document method
        kDoc.add("HTTP method: %L\n\n", verb.uppercase())

        if (bodyParams.isNotEmpty()) {
            kDoc.add("Request body:\n")
            bodyParams.forEach {
                kDoc.add("\t[%L] %L\n\n", it.type, it.description?.trimIndent().orEmpty()).build()
            }
        }

        // document the response
        val happyPathResponse = operation.happyPathResponse(packages.base)
        kDoc.add("Response:\n")
        if (happyPathResponse.isUnit()) {
            kDoc.add("\tA successful request returns an HTTP ${operation.responses.keys.first()} response with an empty body.\n\n")
        } else {
            kDoc.add(
                "\tA successful request returns an HTTP ${operation.responses.keys.first()} response with [%L] in the response body.\n\n",
                happyPathResponse.toString(),
            )
        }

        // document parameters
        if (headerParams.isNotEmpty()) {
            kDoc.add("Request headers:\n")
            headerParams.forEach {
                kDoc.add("\t\"%L\" (%L) %L\n", it.originalName, if (it.isRequired) "required" else "optional", it.description?.trimIndent().orEmpty()).build()
            }
            kDoc.add("\n")
        }
        if ((pathParams + queryParams).isNotEmpty()) {
            kDoc.add("Request parameters:\n")
            (pathParams + queryParams).forEach {
                kDoc.add("\t @param %L %L\n", it.name.toKCodeName(), it.description?.trimIndent().orEmpty()).build()
            }
        }

        return kDoc.build()
    }
}
