package com.cjbooms.fabrikt.model

import com.squareup.kotlinpoet.AnnotationSpec

internal object DeprecationAnnotations {
    fun operation(): AnnotationSpec = annotation("This API operation is deprecated.")

    fun parameter(): AnnotationSpec = annotation("This API parameter is deprecated.")

    fun schema(): AnnotationSpec = annotation("This API schema is deprecated.")

    fun property(): AnnotationSpec = annotation("This API property is deprecated.")

    private fun annotation(message: String): AnnotationSpec =
        AnnotationSpec
            .builder(Deprecated::class)
            .addMember("message = %S", message)
            .build()
}
