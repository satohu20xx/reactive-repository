package com.kkagurazaka.reactive.repository.sample

import com.kkagurazaka.reactive.repository.annotation.InMemoryRepository
import kotlinx.coroutines.flow.Flow

@InMemoryRepository(KotlinDataClassEntityWithoutDefault::class)
interface KotlinDataClassEntityWithoutDefaultInMemoryRepository {

    fun get(): KotlinDataClassEntityWithoutDefault?

    fun observe(): Flow<KotlinDataClassEntityWithoutDefault?>

    fun store(entity: KotlinDataClassEntityWithoutDefault?)
}
