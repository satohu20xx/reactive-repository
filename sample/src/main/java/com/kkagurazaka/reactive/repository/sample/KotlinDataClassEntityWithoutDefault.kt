package com.kkagurazaka.reactive.repository.sample

import com.kkagurazaka.reactive.repository.annotation.InMemoryEntity

@InMemoryEntity
data class KotlinDataClassEntityWithoutDefault(
    val isVeteran: Boolean = false,
    val someStr: String = "",
    val age: Int = 0,
    val pie: Float = 0f,
    val amount: Long = 0,
    val strList: Set<String> = emptySet()
)
