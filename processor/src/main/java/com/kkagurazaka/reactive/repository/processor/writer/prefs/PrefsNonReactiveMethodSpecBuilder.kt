package com.kkagurazaka.reactive.repository.processor.writer.prefs

import com.kkagurazaka.reactive.repository.processor.definition.MethodDefinition
import com.kkagurazaka.reactive.repository.processor.definition.prefs.PrefsEntityDefinition
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier

object PrefsNonReactiveMethodSpecBuilder {

    fun build(
        definition: MethodDefinition<PrefsEntityDefinition>,
        hasCoroutineFlow: Boolean
    ): FunSpec? {
        val entityDefinition = definition.entityDefinition

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
                builder.returns(entityDefinition.className)
                    .addCode("return getImpl()")
            }

            is MethodDefinition.Type.NullableSetter -> {
                builder.setupAsNullableSetter(
                    entityDefinition,
                    type.parameterName,
                    hasCoroutineFlow
                )
            }

            is MethodDefinition.Type.NonNullSetter -> {
                builder.setupAsNonNullSetter(entityDefinition, type.parameterName, hasCoroutineFlow)
            }

            is MethodDefinition.Type.PlatFormTypeSetter -> {
                builder.setupAsNullableSetter(
                    entityDefinition,
                    type.parameterName,
                    hasCoroutineFlow
                )
            }
        }.build()
    }

    fun buildPrivateGetter(name: String, entityDefinition: PrefsEntityDefinition): FunSpec =
        FunSpec.builder(name)
            .addModifiers(KModifier.PRIVATE)
            .returns(entityDefinition.className)
            .addGetPreferenceCode(entityDefinition)
            .build()

    private fun FunSpec.Builder.setupAsNullableSetter(
        entityDefinition: PrefsEntityDefinition,
        parameterName: String,
        hasCoroutineFlow: Boolean
    ): FunSpec.Builder =
        addParameter(parameterName, entityDefinition.className.copy(nullable = true))
            .addClearPreferenceCode(entityDefinition, parameterName, hasCoroutineFlow)
            .addStoreToPreferenceCode(entityDefinition, parameterName)
            .apply {
                if (hasCoroutineFlow) {
                    addProcessorOnNextCode(parameterName)
                }
            }

    private fun FunSpec.Builder.setupAsNonNullSetter(
        entityDefinition: PrefsEntityDefinition,
        parameterName: String,
        hasCoroutineFlow: Boolean
    ): FunSpec.Builder =
        addParameter(parameterName, entityDefinition.className)
            .addStoreToPreferenceCode(entityDefinition, parameterName)
            .apply {
                if (hasCoroutineFlow) {
                    addProcessorOnNextCode(parameterName)
                }
            }

    private fun FunSpec.Builder.addGetPreferenceCode(entityDefinition: PrefsEntityDefinition): FunSpec.Builder =
        addCode(PrefsEntityStatementBuilder.buildGetStatement(entityDefinition))

    private fun FunSpec.Builder.addClearPreferenceCode(
        entityDefinition: PrefsEntityDefinition,
        parameterName: String,
        hasCoroutineProcessor: Boolean
    ): FunSpec.Builder =
        beginControlFlow("if (%L == null)", parameterName)
            .addCode(
                PrefsEntityStatementBuilder.buildClearStatement(
                    entityDefinition.accessorType,
                    entityDefinition.commitOnSave
                )
            )
            .apply {
                if (hasCoroutineProcessor) {
                    addStatement("stateFlow.value = %T()", entityDefinition.className)
                }
            }
            .addStatement("return")
            .endControlFlow()

    private fun FunSpec.Builder.addStoreToPreferenceCode(
        entityDefinition: PrefsEntityDefinition,
        parameterName: String
    ): FunSpec.Builder =
        addCode(
            PrefsEntityStatementBuilder.buildStoreStatement(
                parameterName,
                entityDefinition.accessorType,
                entityDefinition.commitOnSave,
                entityDefinition.typeAdapter?.isInstanceRequired ?: false
            )
        )

    private fun FunSpec.Builder.addProcessorOnNextCode(parameterName: String): FunSpec.Builder =
        addStatement("stateFlow.value = %L", parameterName)
}
