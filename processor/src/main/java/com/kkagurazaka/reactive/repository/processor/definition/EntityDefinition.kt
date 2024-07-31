package com.kkagurazaka.reactive.repository.processor.definition

import com.kkagurazaka.reactive.repository.processor.ProcessingContext
import com.kkagurazaka.reactive.repository.processor.tools.AnnotationHandle
import com.kkagurazaka.reactive.repository.processor.tools.hasEmptyConstructor
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asClassName
import javax.lang.model.element.TypeElement

abstract class EntityDefinition<E : Annotation>(
    context: ProcessingContext,
    val element: TypeElement
) {

    val className: ClassName = element.asClassName()
    val hasEmptyConstructor: Boolean = element.hasEmptyConstructor

    protected abstract val annotationHandle: AnnotationHandle<E>

    protected open fun init() {}
}
