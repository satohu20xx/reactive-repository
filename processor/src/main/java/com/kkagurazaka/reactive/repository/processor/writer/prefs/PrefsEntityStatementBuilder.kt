package com.kkagurazaka.reactive.repository.processor.writer.prefs

import com.kkagurazaka.reactive.repository.processor.definition.prefs.KeyDefinition
import com.kkagurazaka.reactive.repository.processor.definition.prefs.PrefsEntityDefinition
import com.kkagurazaka.reactive.repository.processor.definition.prefs.TypeAdapterDefinition
import com.squareup.kotlinpoet.CodeBlock

object PrefsEntityStatementBuilder {

    fun buildGetStatement(entityDefinition: PrefsEntityDefinition): CodeBlock =
        CodeBlock.builder()
            .apply {
                val isTypeAdapterInstanceRequired =
                    entityDefinition.typeAdapter?.isInstanceRequired ?: false

                when (val accessorType = entityDefinition.accessorType) {
                    is PrefsEntityDefinition.AccessorType.Fields -> {
                        addStatement(
                            "val value = %T()",
                            entityDefinition.className
                        )
                        accessorType.fields.forEach { def ->
                            val adapterMethod = def.type.typeAdapterMethod

                            val getDefaultCode = CodeBlock.builder()
                                .add("defaultValue.%L", def.key)
                                .build()
                                .wrapByToPrefs(adapterMethod, isTypeAdapterInstanceRequired)

                            val getCode = CodeBlock.builder()
                                .add(
                                    "getPreferences().%L(%S, ",
                                    def.type.prefsType.toGetMethod(),
                                    def.key
                                )
                                .add(getDefaultCode)
                                .add(")")
                                .build()
                                .wrapByToType(adapterMethod, isTypeAdapterInstanceRequired)

                            val setCode = CodeBlock.builder()
                                .add("value.%L = ", def.key)
                                .add(getCode)
                                .build()

                            add(setCode)
                        }
                        addStatement("return value")
                    }

                    is PrefsEntityDefinition.AccessorType.GettersAndSetterConstructor -> {
                        add(
                            CodeBlock.builder()
                                .apply {
                                    add("return %T(", entityDefinition.className).indent()
                                    accessorType.getters.forEachIndexed { i, def ->
                                        val adapterMethod = def.type.typeAdapterMethod

                                        add("\n")

                                        val getDefaultCode = CodeBlock.builder()
                                            .add("defaultValue.%L", def.parameterName)
                                            .build()
                                            .wrapByToPrefs(
                                                adapterMethod,
                                                isTypeAdapterInstanceRequired
                                            )

                                        val getCode = if (def.type.prefsType.nullable) {
                                            CodeBlock.builder()
                                                .add(
                                                    "getPreferences().%L(%S, null) ?: ",
                                                    def.type.prefsType.toGetMethod(),
                                                    def.key
                                                )
                                                .add(getDefaultCode)
                                                .build()
                                                .wrapByToType(
                                                    adapterMethod,
                                                    isTypeAdapterInstanceRequired
                                                )
                                        } else {
                                            CodeBlock.builder()
                                                .add(
                                                    "getPreferences().%L(%S, %L)",
                                                    def.type.prefsType.toGetMethod(),
                                                    def.key,
                                                    getDefaultCode
                                                )
                                                .build()
                                                .wrapByToType(
                                                    adapterMethod,
                                                    isTypeAdapterInstanceRequired
                                                )
                                        }

                                        add(getCode)

                                        if (i != accessorType.getters.lastIndex) {
                                            add(",")
                                        }
                                    }
                                    unindent().add("\n)")
                                        .build()
                                }
                                .build()
                        )
                    }

                    is PrefsEntityDefinition.AccessorType.GettersAndSetters -> {
                        addStatement(
                            "%T value = %T()",
                            entityDefinition.className,
                            entityDefinition.className
                        )
                        accessorType.getters.zip(accessorType.setters)
                            .forEach { (getterDef, setterDef) ->
                                val adapterMethod = getterDef.type.typeAdapterMethod

                                val getDefaultCode = CodeBlock.builder()
                                    .add("defaultValue.%L()", getterDef.parameterName)
                                    .build()
                                    .wrapByToPrefs(adapterMethod, isTypeAdapterInstanceRequired)

                                val getCode = CodeBlock.builder()
                                    .add(
                                        "getPreferences().%L(%S, ",
                                        getterDef.type.prefsType.toGetMethod(),
                                        getterDef.key
                                    )
                                    .add(getDefaultCode)
                                    .add(")")
                                    .build()
                                    .wrapByToType(adapterMethod, isTypeAdapterInstanceRequired)

                                val setCode = CodeBlock.builder()
                                    .add("value.%L(", setterDef.parameterName)
                                    .add(getCode)
                                    .add(")")
                                    .build()

                                add(setCode)
                            }
                        addStatement("return value")
                    }
                }
            }
            .build()

    fun buildStoreStatement(
        parameterName: String,
        accessorType: PrefsEntityDefinition.AccessorType,
        commitOnSave: Boolean,
        isTypeAdapterInstanceRequired: Boolean
    ): CodeBlock =
        CodeBlock.builder()
            .add(
                CodeBlock.builder()
                    .add("getPreferences().edit()")
                    .apply {
                        when (accessorType) {
                            is PrefsEntityDefinition.AccessorType.Fields -> {
                                accessorType.fields
                            }

                            is PrefsEntityDefinition.AccessorType.GettersAndSetterConstructor -> {
                                accessorType.getters
                            }

                            is PrefsEntityDefinition.AccessorType.GettersAndSetters -> {
                                accessorType.getters
                            }
                        }.forEach { def ->
                            val adapterMethod = def.type.typeAdapterMethod

                            val getCode = CodeBlock.builder()
                                .add(
                                    "%L.%L",
                                    parameterName,
                                    def.parameterName
                                )
                                .build()
                                .wrapByToPrefs(adapterMethod, isTypeAdapterInstanceRequired)

                            val setCode = CodeBlock.builder()
                                .add(
                                    "\n.%L(%S, ",
                                    def.type.prefsType.toPutMethod(),
                                    def.key
                                )
                                .add(getCode)
                                .add(")")
                                .build()
                            add(setCode)
                        }

                        if (commitOnSave) {
                            add("\n.commit()")
                        } else {
                            add("\n.apply()")
                        }
                    }
                    .add("\n")
                    .build()
            )
            .build()

    fun buildClearStatement(
        accessorType: PrefsEntityDefinition.AccessorType,
        commitOnSave: Boolean
    ): CodeBlock =
        CodeBlock.builder()
            .add(
                CodeBlock.builder()
                    .add("getPreferences().edit()")
                    .apply {
                        when (accessorType) {
                            is PrefsEntityDefinition.AccessorType.Fields -> {
                                accessorType.fields.map { it.key }
                            }

                            is PrefsEntityDefinition.AccessorType.GettersAndSetterConstructor -> {
                                accessorType.getters.map { it.key }
                            }

                            is PrefsEntityDefinition.AccessorType.GettersAndSetters -> {
                                accessorType.getters.map { it.key }
                            }
                        }.forEach { key ->
                            add("\n.remove(%S)", key)
                        }

                        if (commitOnSave) {
                            add("\n.commit()")
                        } else {
                            add("\n.apply()")
                        }
                    }
                    .add("\n")
                    .build()
            )
            .build()

    private fun CodeBlock.wrapByToType(
        adapterMethod: TypeAdapterDefinition.AdapterMethodPair?,
        isTypeAdapterInstanceRequired: Boolean
    ): CodeBlock =
        if (adapterMethod?.toTypeMethod?.simpleName != null) {
            if (isTypeAdapterInstanceRequired) {
                CodeBlock.builder()
                    .add("typeAdapter.%L(", adapterMethod.toTypeMethod.simpleName.toString())
                    .add(this)
                    .add(")")
                    .build()
            } else {
                CodeBlock.builder()
                    .add(
                        "%T.%L(",
                        adapterMethod.className,
                        adapterMethod.toTypeMethod.simpleName.toString()
                    )
                    .add(this)
                    .add(")")
                    .build()
            }
        } else {
            this
        }

    private fun CodeBlock.wrapByToPrefs(
        adapterMethod: TypeAdapterDefinition.AdapterMethodPair?,
        isTypeAdapterInstanceRequired: Boolean
    ): CodeBlock =
        if (adapterMethod?.toPrefsMethod?.simpleName != null) {
            if (isTypeAdapterInstanceRequired) {
                CodeBlock.builder()
                    .add("typeAdapter.%L(", adapterMethod.toPrefsMethod.simpleName.toString())
                    .add(this)
                    .add(")")
                    .build()
            } else {
                CodeBlock.builder()
                    .add(
                        "%T.%L(",
                        adapterMethod.className,
                        adapterMethod.toPrefsMethod.simpleName.toString()
                    )
                    .add(this)
                    .add(")")
                    .build()
            }
        } else {
            this
        }

    private fun KeyDefinition.PrefsType.toGetMethod(): String =
        when (this) {
            KeyDefinition.PrefsType.BOOLEAN -> "getBoolean"
            KeyDefinition.PrefsType.STRING -> "getString"
            KeyDefinition.PrefsType.INT -> "getInt"
            KeyDefinition.PrefsType.FLOAT -> "getFloat"
            KeyDefinition.PrefsType.LONG -> "getLong"
            KeyDefinition.PrefsType.STRING_SET -> "getStringSet"
        }

    private fun KeyDefinition.PrefsType.toPutMethod(): String =
        when (this) {
            KeyDefinition.PrefsType.BOOLEAN -> "putBoolean"
            KeyDefinition.PrefsType.STRING -> "putString"
            KeyDefinition.PrefsType.INT -> "putInt"
            KeyDefinition.PrefsType.FLOAT -> "putFloat"
            KeyDefinition.PrefsType.LONG -> "putLong"
            KeyDefinition.PrefsType.STRING_SET -> "putStringSet"
        }
}
