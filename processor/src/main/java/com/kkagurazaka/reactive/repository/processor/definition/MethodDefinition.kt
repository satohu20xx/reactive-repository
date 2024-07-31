package com.kkagurazaka.reactive.repository.processor.definition

import com.kkagurazaka.reactive.repository.processor.ProcessingContext
import com.kkagurazaka.reactive.repository.processor.exception.ProcessingException
import com.kkagurazaka.reactive.repository.processor.tools.Types
import com.kkagurazaka.reactive.repository.processor.tools.isNonNullAnnotated
import com.kkagurazaka.reactive.repository.processor.tools.isNullableAnnotated
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.asTypeName
import javax.lang.model.element.ExecutableElement

class MethodDefinition<ED : EntityDefinition<out Annotation>>(
    context: ProcessingContext,
    val element: ExecutableElement,
    val entityDefinition: ED
) {

    val methodName: String
    val type: Type

    init {
        methodName = element.simpleName.toString()

        val entityClassName = entityDefinition.className
        val isEmptyParameter = element.parameters.isEmpty()
        val returnTypeName = element.returnType.asTypeName()

        type = when {
            isEmptyParameter -> when {
                // getter has no parameters and returns entity class
                returnTypeName == entityClassName -> {
                    when {
                        element.isNullableAnnotated -> Type.NullableGetter
                        element.isNonNullAnnotated -> Type.NonNullGetter
                        else -> Type.PlatFormTypeGetter
                    }
                }
                // method returning Rx Observable has no parameters and return Observable<Entity>
                returnTypeName is ParameterizedTypeName && returnTypeName.rawType == Types.coroutineFlow -> {
                    when {
                        element.isNullableAnnotated -> {
                            throw ProcessingException(
                                "Method returning Observable cannot be annotated with @Nullable",
                                element
                            )
                        }

                        returnTypeName == Types.coroutineFlow.parameterizedBy(entityClassName) -> {
                            Type.CoroutineFlow
                        }

                        else -> {
                            val typeParameter = returnTypeName.typeArguments.single()
                            throw ProcessingException(
                                "Expected return type is Observable<$entityClassName> but actual is Observable<$typeParameter> at ${element.simpleName}()",
                                element
                            )
                        }
                    }
                }

                else -> {
                    throw ProcessingException(
                        "Expected return type is $entityClassName, Flow<$entityClassName> but actual is $returnTypeName at ${element.simpleName}()",
                        element
                    )
                }
            }
            // setter take entity class as parameter and returns void
            element.parameters.size == 1 &&
                    element.parameters.single().asType().asTypeName() == entityClassName -> {
                val parameter = element.parameters.single()
                val name = parameter.simpleName.toString()
                when {
                    parameter.isNullableAnnotated -> Type.NullableSetter(name)
                    parameter.isNonNullAnnotated -> Type.NonNullSetter(name)
                    else -> Type.PlatFormTypeSetter(name)
                }
            }

            else -> {
                throw ProcessingException(
                    "Signature of ${element.simpleName} is not supported",
                    element
                )
            }
        }
    }

    sealed interface Type {
        data object NullableGetter : Type
        data object NonNullGetter : Type
        data object PlatFormTypeGetter : Type
        data object CoroutineFlow : Type
        data class NullableSetter(val parameterName: String) : Type
        data class NonNullSetter(val parameterName: String) : Type
        data class PlatFormTypeSetter(val parameterName: String) : Type
    }
}
