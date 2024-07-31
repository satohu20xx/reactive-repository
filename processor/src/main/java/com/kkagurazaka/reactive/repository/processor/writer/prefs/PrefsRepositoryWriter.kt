package com.kkagurazaka.reactive.repository.processor.writer.prefs

import com.kkagurazaka.reactive.repository.processor.ProcessingContext
import com.kkagurazaka.reactive.repository.processor.definition.MethodDefinition
import com.kkagurazaka.reactive.repository.processor.definition.prefs.PrefsEntityDefinition
import com.kkagurazaka.reactive.repository.processor.definition.prefs.PrefsRepositoryDefinition
import com.kkagurazaka.reactive.repository.processor.exception.ProcessingException
import com.kkagurazaka.reactive.repository.processor.tools.Types
import com.kkagurazaka.reactive.repository.processor.writer.CoroutineMethodSpecBuilder
import com.kkagurazaka.reactive.repository.processor.writer.RepositoryWriter
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

class PrefsRepositoryWriter(context: ProcessingContext, definition: PrefsRepositoryDefinition) :
    RepositoryWriter<PrefsRepositoryDefinition>(context, definition) {

    override fun TypeSpec.Builder.setup(): TypeSpec.Builder {
        val entityDefinition = definition.entityDefinition
        val entityClassName = entityDefinition.className
        val hasCoroutineMethods = definition.hasCoroutineMethods

        // private final Context context;
        val applicationContext = PropertySpec
            .builder("context", Types.androidContext)
            .build()
        addProperty(applicationContext)

        // private final AtomicReference<SharedPreferences> preferences = new AtomicReference<>();
        val atomicPreferencesClassName = Types.atomicReference(Types.sharedPreferences)
        val preferences = PropertySpec
            .builder("preferences", atomicPreferencesClassName, KModifier.PRIVATE)
            .initializer("%T()", atomicPreferencesClassName)
            .build()
        addProperty(preferences)

        // private final EntityClass defaultValue = new EntityClass();
        val defaultValue = PropertySpec
            .builder("defaultValue", entityClassName, KModifier.PRIVATE)
            .initializer("%T()", entityClassName)
            .build()
        addProperty(defaultValue)

        // private final TypeAdapter typeAdapter;
        entityDefinition.typeAdapter?.takeIf { it.isInstanceRequired }?.let { def ->
            val typeAdapter = PropertySpec
                .builder("typeAdapter", def.className, KModifier.PRIVATE)
                .build()
            addProperty(typeAdapter)
        }

        addFunction(buildConstructorMethodSpec(entityDefinition))

        definition.methodDefinitions.mapNotNull {
            PrefsNonReactiveMethodSpecBuilder.build(it, hasCoroutineMethods)
        }.forEach {
            addFunction(it)
        }

        if (hasCoroutineMethods) {
            addProperty(
                PropertySpec
                    .builder(
                        "stateFlow",
                        Types.coroutineMutableStateFlow
                            .parameterizedBy(entityDefinition.className),
                        KModifier.PRIVATE
                    )
                    .delegate(
                        CodeBlock.builder()
                            .beginControlFlow("lazy", LazyThreadSafetyMode::class.java)
                            .add("MutableStateFlow(getImpl())")
                            .endControlFlow()
                            .build()
                    )
                    .build()
            )
            definition.methodDefinitions.mapNotNull {
                CoroutineMethodSpecBuilder.build(it, false)
            }.forEach {
                addFunction(it)
            }
        }

        val method = PrefsNonReactiveMethodSpecBuilder.buildPrivateGetter(
            "getImpl",
            entityDefinition
        )
        addFunction(method)

        addFunction(buildGetPreferencesMethodSpec(entityDefinition))

        return this
    }

    override fun verify() {
        if (definition.has<MethodDefinition.Type.NullableGetter>()) {
            throw ProcessingException(
                "@PrefsRepository does not accept @Nullable getter",
                definition.element
            )
        }
    }

    private fun buildConstructorMethodSpec(entityDefinition: PrefsEntityDefinition): FunSpec =
        FunSpec.constructorBuilder()
            .addModifiers(KModifier.PUBLIC)
            .addParameter("context", Types.androidContext)
            .apply {
                entityDefinition.typeAdapter?.takeIf { it.isInstanceRequired }?.let {
                    addParameter("typeAdapter", it.className)
                }
            }
            .addCode(
                CodeBlock.builder()
                    .addStatement("this.context = context.getApplicationContext()")
                    .apply {
                        entityDefinition.typeAdapter?.takeIf { it.isInstanceRequired }?.let {
                            addStatement("this.typeAdapter = typeAdapter")
                        }
                    }
                    .build()
            )
            .build()

    private fun buildGetPreferencesMethodSpec(entityDefinition: PrefsEntityDefinition): FunSpec =
        FunSpec.builder("getPreferences")
            .addModifiers(KModifier.PRIVATE)
            .returns(Types.sharedPreferences)
            .addCode(
                CodeBlock.builder()
                    .addStatement("var result = preferences.get()", Types.sharedPreferences)
                    .beginControlFlow("if (result == null)")
                    .apply {
                        when (val preferencesType = entityDefinition.preferencesType) {
                            is PrefsEntityDefinition.PreferencesType.Default -> {
                                addStatement(
                                    "result = %T.getDefaultSharedPreferences(context)",
                                    Types.preferenceManager
                                )
                            }

                            is PrefsEntityDefinition.PreferencesType.Named -> {
                                addStatement(
                                    "result = context.getSharedPreferences(%S, %T.MODE_PRIVATE)",
                                    preferencesType.name,
                                    Types.androidContext
                                )
                            }
                        }
                        beginControlFlow("if(!preferences.compareAndSet(null, result))")
                            .addStatement("return preferences.get()")
                            .endControlFlow()
                    }
                    .endControlFlow()
                    .addStatement("return result")
                    .build()
            )
            .build()
}
