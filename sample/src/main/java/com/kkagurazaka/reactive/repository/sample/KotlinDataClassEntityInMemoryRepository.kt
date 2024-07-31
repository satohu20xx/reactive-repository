package com.kkagurazaka.reactive.repository.sample

import com.kkagurazaka.reactive.repository.annotation.InMemoryRepository
import kotlinx.coroutines.flow.Flow

@InMemoryRepository(KotlinDataClassEntity::class)
interface KotlinDataClassEntityInMemoryRepository {

    fun get(): KotlinDataClassEntity?

    fun observe(): Flow<KotlinDataClassEntity?>

    fun store(entity: KotlinDataClassEntity?)
}
