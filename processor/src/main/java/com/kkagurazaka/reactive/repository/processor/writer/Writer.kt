package com.kkagurazaka.reactive.repository.processor.writer

import com.kkagurazaka.reactive.repository.processor.ProcessingContext
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec

abstract class Writer(protected val context: ProcessingContext) {

    abstract val packageName: String

    abstract fun buildTypeSpec(): TypeSpec

    fun buildJavaFile(): FileSpec =
        FileSpec.get(packageName, buildTypeSpec())
}
