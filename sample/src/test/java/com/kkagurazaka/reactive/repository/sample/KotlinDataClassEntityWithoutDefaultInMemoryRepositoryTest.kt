package com.kkagurazaka.reactive.repository.sample

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class KotlinDataClassEntityWithoutDefaultInMemoryRepositoryTest {

    private val dispatcher = StandardTestDispatcher()

    private lateinit var repository: KotlinDataClassEntityWithoutDefaultInMemoryRepository

    @Before
    fun setup() {
        repository = KotlinDataClassEntityWithoutDefaultInMemoryRepositoryImpl()
    }

    @Test
    fun `get - default value`() {
        val result = repository.get()

        assertThat(result).isNull()
    }

    @Test
    fun `store and get`() {
        val newEntity = KotlinDataClassEntityWithoutDefault(
            isVeteran = true,
            someStr = "some some",
            age = 24,
            pie = 3f,
            amount = 12L,
            strList = setOf("1", "2", "3")
        )

        repository.store(newEntity)

        val result = repository.get()

        assertThat(result).isNotNull()
        result!!.run {
            assertThat(isVeteran).isTrue()
            assertThat(someStr).isEqualTo("some some")
            assertThat(age).isEqualTo(24)
            assertThat(pie).isEqualTo(3f)
            assertThat(amount).isEqualTo(12L)
            assertThat(strList).isEqualTo(setOf("1", "2", "3"))
        }
    }

    @Test
    fun `observe initially - never emit`() = runTest(dispatcher) {
        val result = repository.observe().firstOrNull()

        assertThat(result).isNull()
    }

    @Test
    fun observe() = runTest(dispatcher) {
        val newEntity = KotlinDataClassEntityWithoutDefault(
            isVeteran = true,
            someStr = "some some",
            age = 24,
            pie = 3f,
            amount = 12L,
            strList = setOf("1", "2", "3")
        )

        repository.store(newEntity)

        val result = repository.get()

        assertThat(result?.isVeteran).isTrue()
        assertThat(result?.someStr).isEqualTo("some some")
        assertThat(result?.age).isEqualTo(24)
        assertThat(result?.pie).isEqualTo(3f)
        assertThat(result?.amount).isEqualTo(12L)
        assertThat(result?.strList).isEqualTo(setOf("1", "2", "3"))
    }
}