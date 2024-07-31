package com.kkagurazaka.reactive.repository.processor.writer

import com.kkagurazaka.reactive.repository.processor.definition.MethodDefinition
import com.kkagurazaka.reactive.repository.processor.tools.Types
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

object CoroutineMethodSpecBuilder {

    fun build(
        definition: MethodDefinition<*>,
        nullable: Boolean
    ): FunSpec? {
        val builder = FunSpec.builder(definition.methodName)
            .addModifiers(KModifier.PUBLIC)
            .addModifiers(KModifier.OVERRIDE)

        return when (definition.type) {
            is MethodDefinition.Type.NullableGetter,
            is MethodDefinition.Type.NonNullGetter,
            is MethodDefinition.Type.PlatFormTypeGetter,
            is MethodDefinition.Type.NullableSetter,
            is MethodDefinition.Type.NonNullSetter,
            is MethodDefinition.Type.PlatFormTypeSetter -> {
                return null
            }

            is MethodDefinition.Type.CoroutineFlow -> {
                builder.returns(
                    Types.coroutineFlow
                        .parameterizedBy(definition.entityDefinition.className.copy(nullable = nullable))
                )
                    .addCode(
                        CodeBlock.builder()
                            .addStatement("return stateFlow")
                            .build()
                    )
            }
        }.build()
    }
}
