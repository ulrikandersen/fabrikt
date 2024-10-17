package com.cjbooms.fabrikt.model

import com.cjbooms.fabrikt.generators.model.JacksonMetadata
import com.cjbooms.fabrikt.generators.model.JacksonMetadata.basePolymorphicType
import com.cjbooms.fabrikt.generators.model.JacksonMetadata.polymorphicSubTypes
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec

object JacksonAnnotations : SerializationAnnotations {
    override val supportsAdditionalProperties = true

    override fun addIgnore(propertySpecBuilder: PropertySpec.Builder): PropertySpec.Builder =
        propertySpecBuilder.addAnnotation(JacksonMetadata.ignore)

    override fun addGetter(funSpecBuilder: FunSpec.Builder): FunSpec.Builder =
        funSpecBuilder.addAnnotation(JacksonMetadata.anyGetter)

    override fun addSetter(funSpecBuilder: FunSpec.Builder): FunSpec.Builder =
        funSpecBuilder.addAnnotation(JacksonMetadata.anySetter)

    override fun addProperty(propertySpecBuilder: PropertySpec.Builder, oasKey: String): PropertySpec.Builder =
        propertySpecBuilder.addAnnotation(JacksonMetadata.jacksonPropertyAnnotation(oasKey))

    override fun addParameter(propertySpecBuilder: PropertySpec.Builder, oasKey: String): PropertySpec.Builder =
        propertySpecBuilder.addAnnotation(JacksonMetadata.jacksonParameterAnnotation(oasKey))

    override fun addClassAnnotation(typeSpecBuilder: TypeSpec.Builder): TypeSpec.Builder =
        typeSpecBuilder

    override fun addBasePolymorphicTypeAnnotation(typeSpecBuilder: TypeSpec.Builder, propertyName: String) =
        typeSpecBuilder.addAnnotation(basePolymorphicType(propertyName))

    override fun addPolymorphicSubTypesAnnotation(typeSpecBuilder: TypeSpec.Builder, mappings: Map<String, TypeName>) =
        typeSpecBuilder.addAnnotation(polymorphicSubTypes(mappings, enumDiscriminator = null))

    override fun addSubtypeMappingAnnotation(typeSpecBuilder: TypeSpec.Builder, mapping: String): TypeSpec.Builder =
        typeSpecBuilder
}