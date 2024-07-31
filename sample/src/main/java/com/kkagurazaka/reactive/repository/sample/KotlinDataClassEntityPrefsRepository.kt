package com.kkagurazaka.reactive.repository.sample

import com.kkagurazaka.reactive.repository.annotation.PrefsRepository
import kotlinx.coroutines.flow.Flow

@PrefsRepository(KotlinDataClassEntity::class)
internal interface KotlinDataClassEntityPrefsRepository {

    fun get(): KotlinDataClassEntity

    fun observe(): Flow<KotlinDataClassEntity>

    fun store(entity: KotlinDataClassEntity?)
}
