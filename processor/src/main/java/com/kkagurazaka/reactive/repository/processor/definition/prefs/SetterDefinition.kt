package com.kkagurazaka.reactive.repository.processor.definition.prefs

import com.kkagurazaka.reactive.repository.processor.ProcessingContext
import com.kkagurazaka.reactive.repository.processor.tools.toLowerSnake
import com.squareup.kotlinpoet.DelicateKotlinPoetApi
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement

class SetterDefinition(
    context: ProcessingContext,
    element: Element,
    typeAdapter: TypeAdapterDefinition?
) : KeyDefinition(context, element, typeAdapter) {

    override val key: String = specifiedKey ?: name.removePrefix("set").toLowerSnake()

    @OptIn(DelicateKotlinPoetApi::class)
    override fun getTargetType(element: Element): TypeName =
        (element as ExecutableElement).parameters.first().asType().asTypeName()
}
