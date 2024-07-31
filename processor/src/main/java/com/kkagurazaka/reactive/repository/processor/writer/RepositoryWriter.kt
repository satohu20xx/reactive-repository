package com.kkagurazaka.reactive.repository.processor.writer

import com.kkagurazaka.reactive.repository.processor.ProcessingContext
import com.kkagurazaka.reactive.repository.processor.definition.EntityDefinition
import com.kkagurazaka.reactive.repository.processor.definition.RepositoryDefinition
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec

abstract class RepositoryWriter<RD : RepositoryDefinition<out Annotation, out EntityDefinition<out Annotation>>>(
    context: ProcessingContext,
    protected val definition: RD
) : Writer(context) {

    final override val packageName: String = definition.packageName

    final override fun buildTypeSpec(): TypeSpec {
        verify()

        return TypeSpec.classBuilder(definition.generatedClassName)
            .addSuperinterface(definition.baseClassName)
            .addModifiers(KModifier.PUBLIC)
            .setup()
            .build()
    }

    protected abstract fun verify()

    protected abstract fun TypeSpec.Builder.setup(): TypeSpec.Builder
}
