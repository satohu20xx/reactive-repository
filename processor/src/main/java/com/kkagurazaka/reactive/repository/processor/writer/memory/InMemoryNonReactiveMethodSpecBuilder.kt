package com.kkagurazaka.reactive.repository.processor.writer.memory

import com.kkagurazaka.reactive.repository.processor.definition.MethodDefinition
import com.kkagurazaka.reactive.repository.processor.definition.memory.InMemoryEntityDefinition
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier

object InMemoryNonReactiveMethodSpecBuilder {

    fun build(
        definition: MethodDefinition<InMemoryEntityDefinition>,
        nullable: Boolean,
    ): FunSpec? {
        val entityDefinition = definition.entityDefinition
        val className = entityDefinition.className.copy(nullable = nullable)

        val builder = FunSpec.builder(definition.methodName)
            .addModifiers(KModifier.PUBLIC)
            .addModifiers(KModifier.OVERRIDE)

        return when (val type = definition.type) {
            is MethodDefinition.Type.CoroutineFlow -> {
                return null
            }

            is MethodDefinition.Type.NullableGetter,
            is MethodDefinition.Type.NonNullGetter,
            is MethodDefinition.Type.PlatFormTypeGetter -> {
                builder.returns(className)
                    .addCode(buildValueGetterCode())
            }

            is MethodDefinition.Type.NullableSetter -> {
                builder.addParameter(type.parameterName, className)
                    .addCode(buildProcessorOnNextCode(type.parameterName))
            }

            is MethodDefinition.Type.NonNullSetter -> {
                builder.addParameter(type.parameterName, className)
                    .addCode(buildProcessorOnNextCode(type.parameterName))
            }

            is MethodDefinition.Type.PlatFormTypeSetter -> {
                builder.addParameter(type.parameterName, className)
                    .addCode(buildProcessorOnNextCode(type.parameterName))
            }
        }.build()
    }

    private fun buildValueGetterCode(): CodeBlock =
        CodeBlock.builder()
            .addStatement("return stateFlow.value")
            .build()

    private fun buildProcessorOnNextCode(parameterName: String): CodeBlock =
        CodeBlock.builder()
            .addStatement("stateFlow.value = %L", parameterName)
            .build()
}
