package com.kkagurazaka.reactive.repository.processor.definition.prefs

import com.kkagurazaka.reactive.repository.annotation.PrefsKey
import com.kkagurazaka.reactive.repository.processor.ProcessingContext
import com.kkagurazaka.reactive.repository.processor.exception.ProcessingException
import com.kkagurazaka.reactive.repository.processor.tools.AnnotationHandle
import com.kkagurazaka.reactive.repository.processor.tools.Types
import com.kkagurazaka.reactive.repository.processor.tools.toLowerCamel
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import javax.lang.model.element.Element

abstract class KeyDefinition(
    context: ProcessingContext,
    val element: Element,
    typeAdapter: TypeAdapterDefinition?
) {

    val name: String = element
        .simpleName
        .toString()
        .let {
            // If internal modifier is used, the prefix "$" is added to name. So remove prefix "$"
            val index = it.indexOf("$")
            if (index >= 0) {
                it.substring(0, index)
            } else {
                it
            }
        }

    val parameterName: String = name
        .removePrefix("get")
        .toLowerCamel()

    abstract val key: String

    protected val specifiedKey: String?

    val type: Type by lazy {
        val targetType = getTargetType(element)

        PrefsType.from(targetType)
            ?.let { prefsType ->
                Type(prefsType, typeAdapterMethod = null)
            }
            ?: run {
                val adapterMethod = typeAdapter?.adapterMethods
                    ?.firstOrNull { it.type == targetType }
                    ?: throw ProcessingException(
                        "PrefsTypeAdapter for $targetType does not found",
                        element
                    )
                val prefsType = PrefsType.from(adapterMethod.prefsType)
                    ?: throw ProcessingException(
                        "${adapterMethod.prefsType} is not supported by SharedPreferences",
                        element
                    )
                Type(prefsType, adapterMethod)
            }
    }

    init {
        val annotationHandle = AnnotationHandle.from<PrefsKey>(element)
            ?: throw ProcessingException(
                "${element.asType().asTypeName()} is not annotated with @PrefsKey",
                element
            )

        specifiedKey = annotationHandle.getOrDefault<String>("value")
            .takeIf { it.isNotBlank() }
    }

    protected abstract fun getTargetType(element: Element): TypeName

    data class Type(
        val prefsType: PrefsType,
        val typeAdapterMethod: TypeAdapterDefinition.AdapterMethodPair?
    )

    enum class PrefsType(val nullable: Boolean) {
        BOOLEAN(false),
        STRING(true),
        INT(false),
        FLOAT(false),
        LONG(false),
        STRING_SET(true);

        companion object {

            fun from(typeName: TypeName): PrefsType? =
                when (typeName) {
                    com.squareup.kotlinpoet.BOOLEAN -> BOOLEAN
                    Types.string -> STRING
                    com.squareup.kotlinpoet.INT -> INT
                    com.squareup.kotlinpoet.FLOAT -> FLOAT
                    com.squareup.kotlinpoet.LONG -> LONG
                    Types.stringSet -> STRING_SET
                    else -> null
                }
        }
    }
}
