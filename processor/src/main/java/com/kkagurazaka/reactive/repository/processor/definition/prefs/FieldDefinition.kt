package com.kkagurazaka.reactive.repository.processor.definition.prefs

import com.kkagurazaka.reactive.repository.processor.ProcessingContext
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import javax.lang.model.element.Element

class FieldDefinition(
    context: ProcessingContext,
    element: Element,
    typeAdapter: TypeAdapterDefinition?
) : KeyDefinition(context, element, typeAdapter) {

    override val key: String = specifiedKey ?: name

    override fun getTargetType(element: Element): TypeName =
        element.asType().asTypeName()
}
