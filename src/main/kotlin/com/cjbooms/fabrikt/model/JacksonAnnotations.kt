package com.cjbooms.fabrikt.model

import com.cjbooms.fabrikt.generators.model.JacksonMetadata
import com.cjbooms.fabrikt.generators.model.JacksonMetadata.JSON_VALUE
import com.cjbooms.fabrikt.generators.model.JacksonMetadata.basePolymorphicType
import com.cjbooms.fabrikt.generators.model.JacksonMetadata.polymorphicSubTypes
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec

object JacksonAnnotations : SerializationAnnotations {
    override val supportsBackingPropertyForDiscriminator = true
    override val supportsAdditionalProperties = true
    override fun addIgnore(propertySpecBuilder: PropertySpec.Builder) =
        propertySpecBuilder.addAnnotation(JacksonMetadata.ignore)

    override fun addGetter(funSpecBuilder: FunSpec.Builder) =
        funSpecBuilder.addAnnotation(JacksonMetadata.anyGetter)

    override fun addSetter(funSpecBuilder: FunSpec.Builder) =
        funSpecBuilder.addAnnotation(JacksonMetadata.anySetter)

    override fun addProperty(propertySpecBuilder: PropertySpec.Builder, oasKey: String) =
        propertySpecBuilder.addAnnotation(JacksonMetadata.jacksonPropertyAnnotation(oasKey))

    override fun addParameter(propertySpecBuilder: PropertySpec.Builder, oasKey: String) =
        propertySpecBuilder.addAnnotation(JacksonMetadata.jacksonParameterAnnotation(oasKey))

    override fun addClassAnnotation(typeSpecBuilder: TypeSpec.Builder) =
        typeSpecBuilder

    override fun addBasePolymorphicTypeAnnotation(typeSpecBuilder: TypeSpec.Builder, propertyName: String) =
        typeSpecBuilder.addAnnotation(basePolymorphicType(propertyName))

    override fun addPolymorphicSubTypesAnnotation(typeSpecBuilder: TypeSpec.Builder, mappings: Map<String, TypeName>, enumDiscriminator: KotlinTypeInfo.Enum?) =
        typeSpecBuilder.addAnnotation(polymorphicSubTypes(mappings, enumDiscriminator))

    override fun addSubtypeMappingAnnotation(typeSpecBuilder: TypeSpec.Builder, mapping: String) =
        typeSpecBuilder

    override fun addEnumPropertyAnnotation(propSpecBuilder: PropertySpec.Builder) =
        propSpecBuilder.addAnnotation(JSON_VALUE)

    override fun addEnumConstantAnnotation(enumSpecBuilder: TypeSpec.Builder, enumValue: String) =
        enumSpecBuilder // not applicable
}
