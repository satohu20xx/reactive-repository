package com.kkagurazaka.reactive.repository.processor.writer.memory

import com.kkagurazaka.reactive.repository.processor.ProcessingContext
import com.kkagurazaka.reactive.repository.processor.definition.MethodDefinition
import com.kkagurazaka.reactive.repository.processor.definition.memory.InMemoryRepositoryDefinition
import com.kkagurazaka.reactive.repository.processor.exception.ProcessingException
import com.kkagurazaka.reactive.repository.processor.tools.Types
import com.kkagurazaka.reactive.repository.processor.writer.CoroutineMethodSpecBuilder
import com.kkagurazaka.reactive.repository.processor.writer.RepositoryWriter
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

class InMemoryRepositoryWriter(
    context: ProcessingContext,
    definition: InMemoryRepositoryDefinition
) : RepositoryWriter<InMemoryRepositoryDefinition>(context, definition) {

    override fun TypeSpec.Builder.setup(): TypeSpec.Builder {
        val entityDefinition = definition.entityDefinition
        val nullable = definition.has<MethodDefinition.Type.NullableSetter>()

        definition.methodDefinitions.mapNotNull {
            InMemoryNonReactiveMethodSpecBuilder.build(it, nullable)
        }.forEach {
            addFunction(it)
        }

        addProperty(
            PropertySpec
                .builder(
                    "stateFlow",
                    Types.coroutineMutableStateFlow.parameterizedBy(
                        entityDefinition.className.copy(nullable = nullable)
                    ),
                    KModifier.PRIVATE
                )
                .apply {
                    if (nullable) {
                        initializer("MutableStateFlow(null)")
                    } else {
                        initializer("MutableStateFlow(%T())", entityDefinition.className)
                    }
                }
                .build()
        )

        definition.methodDefinitions.mapNotNull {
            CoroutineMethodSpecBuilder.build(it, nullable)
        }.forEach {
            addFunction(it)
        }

        return this
    }

    override fun verify() {
        if (definition.has<MethodDefinition.Type.NonNullGetter>() &&
            definition.has<MethodDefinition.Type.NullableSetter>()
        ) {
            throw ProcessingException(
                "@InMemoryRepository does not accept both of @NonNull getter and @Nullable setter",
                definition.element
            )
        }
    }
}
