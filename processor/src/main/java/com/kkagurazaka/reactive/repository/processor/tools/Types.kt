package com.kkagurazaka.reactive.repository.processor.tools

import com.kkagurazaka.reactive.repository.annotation.PrefsEntity
import com.kkagurazaka.reactive.repository.annotation.PrefsKey
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.DelicateKotlinPoetApi
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.asClassName

@OptIn(DelicateKotlinPoetApi::class)
object Types {

    val annotationNonNullJetBrains: ClassName =
        ClassName("org.jetbrains.annotations", "NotNull")
    val annotationNullableJetBrains: ClassName =
        ClassName("org.jetbrains.annotations", "Nullable")
    val annotationNonNullAndroidX: ClassName =
        ClassName("androidx.annotation", "NonNull")
    val annotationNullableAndroidX: ClassName =
        ClassName("androidx.annotation", "Nullable")
    val annotationNonNullSupport: ClassName =
        ClassName("android.support.annotation", "NonNull")
    val annotationNullableSupport: ClassName =
        ClassName("android.support.annotation", "Nullable")

    val string: ClassName =
        java.lang.String::class.java.asClassName()
    val stringSet: ParameterizedTypeName =
        Set::class.java.parameterizedBy(String::class.java)

    val androidContext: ClassName =
        ClassName("android.content", "Context")
    val sharedPreferences: ClassName =
        ClassName("android.content", "SharedPreferences")
    val preferenceManager: ClassName =
        ClassName("android.preference", "PreferenceManager")

    val coroutineFlow: ClassName =
        ClassName("kotlinx.coroutines.flow", "Flow")
    val coroutineMutableStateFlow =
        ClassName("kotlinx.coroutines.flow", "MutableStateFlow")

    val prefsKey: ClassName =
        PrefsKey::class.java.asClassName()
    val defaultTypeAdapter: ClassName =
        PrefsEntity.DEFAULT_ADAPTER::class.java.asClassName()

    fun atomicReference(className: ClassName): ParameterizedTypeName =
        ClassName("java.util.concurrent.atomic", "AtomicReference")
            .parameterizedBy(className)
}
