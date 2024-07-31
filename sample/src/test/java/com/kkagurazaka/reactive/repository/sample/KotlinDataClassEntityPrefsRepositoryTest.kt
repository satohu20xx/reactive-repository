package com.kkagurazaka.reactive.repository.sample

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class KotlinDataClassEntityPrefsRepositoryTest {

    private val dispatcher = StandardTestDispatcher()

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private lateinit var preferences: SharedPreferences
    private lateinit var repository: KotlinDataClassEntityPrefsRepository

    @Before
    fun setup() {
        preferences = context.getSharedPreferences("kotlin_data_class_entity", Context.MODE_PRIVATE)
            .apply { edit().clear().commit() }
        repository = KotlinDataClassEntityPrefsRepositoryImpl(context)
    }

    @Test
    fun `get - default value`() {
        val result = repository.get()

        result.run {
            assertThat(isVeteran).isFalse()
            assertThat(someStr).isNull()
            assertThat(age).isEqualTo(-1)
            assertThat(pie).isEqualTo(3.1415f)
            assertThat(amount).isEqualTo(123456789L)
            assertThat(strList).isEmpty()
            assertThat(someClassList).isEqualTo(listOf(SomeClass("initial")))
        }
    }

    @Test
    fun `get - changed value`() {
        preferences.edit()
            .putBoolean("is_veteran", true)
            .putString("some_str", "some some")
            .putInt("age", 24)
            .putFloat("pie", 3f)
            .putLong("amount", 12L)
            .putStringSet("str_list", setOf("1", "2", "3"))
            .putString("some_class_list", "0,1,2")
            .commit()

        val result = repository.get()

        result.run {
            assertThat(isVeteran).isTrue()
            assertThat(someStr).isEqualTo("some some")
            assertThat(age).isEqualTo(24)
            assertThat(pie).isEqualTo(3f)
            assertThat(amount).isEqualTo(12L)
            assertThat(strList).isEqualTo(setOf("1", "2", "3"))
            assertThat(someClassList).isEqualTo(List(3) { SomeClass("$it") })
        }
    }

    @Test
    fun `observe initially - default value`() = runTest(dispatcher) {
        val result = repository.observe().first()

        assertThat(result.isVeteran).isFalse()
        assertThat(result.someStr).isNull()
        assertThat(result.age).isEqualTo(-1)
        assertThat(result.pie).isEqualTo(3.1415f)
        assertThat(result.amount).isEqualTo(123456789L)
        assertThat(result.strList).isEmpty()
        assertThat(result.someClassList).isEqualTo(listOf(SomeClass("initial")))
    }

    @Test
    fun `observe initially - changed value`() = runTest(dispatcher) {
        preferences.edit()
            .putBoolean("is_veteran", true)
            .putString("some_str", "some some")
            .putInt("age", 24)
            .putFloat("pie", 3f)
            .putLong("amount", 12L)
            .putStringSet("str_list", setOf("1", "2", "3"))
            .putString("some_class_list", "0,1,2")
            .commit()

        val result = repository.observe().first()

        assertThat(result.isVeteran).isTrue()
        assertThat(result.someStr).isEqualTo("some some")
        assertThat(result.age).isEqualTo(24)
        assertThat(result.pie).isEqualTo(3f)
        assertThat(result.amount).isEqualTo(12L)
        assertThat(result.strList).isEqualTo(setOf("1", "2", "3"))
        assertThat(result.someClassList).isEqualTo(List(3) { SomeClass("$it") })
    }

    @Test
    fun observe() {
        val newEntity = KotlinDataClassEntity(
            isVeteran = true,
            someStr = "some some",
            age = 24,
            pie = 3f,
            amount = 12L,
            strList = setOf("1", "2", "3"),
            someClassList = List(3) { SomeClass("$it") }
        )

        repository.store(newEntity)

        val result = repository.get()

        assertThat(result.isVeteran).isTrue()
        assertThat(result.someStr).isEqualTo("some some")
        assertThat(result.age).isEqualTo(24)
        assertThat(result.pie).isEqualTo(3f)
        assertThat(result.amount).isEqualTo(12L)
        assertThat(result.strList).isEqualTo(setOf("1", "2", "3"))
        assertThat(result.someClassList).isEqualTo(List(3) { SomeClass("$it") })
    }

    @Test
    fun store() {
        val newEntity = KotlinDataClassEntity(
            isVeteran = true,
            someStr = "some some",
            age = 24,
            pie = 3f,
            amount = 12L,
            strList = setOf("1", "2", "3"),
            someClassList = List(3) { SomeClass("$it") }
        )

        repository.store(newEntity)

        assertThat(preferences.getBoolean("is_veteran", false)).isTrue()
        assertThat(preferences.getString("some_str", null)).isEqualTo("some some")
        assertThat(preferences.getInt("age", 0)).isEqualTo(24)
        assertThat(preferences.getFloat("pie", 0f)).isEqualTo(3f)
        assertThat(preferences.getLong("amount", 0L)).isEqualTo(12L)
        assertThat(preferences.getStringSet("str_list", null)).isEqualTo(setOf("1", "2", "3"))
        assertThat(preferences.getString("some_class_list", null)).isEqualTo("0,1,2")
    }

    @Test
    fun `store - null`() {
        preferences.edit()
            .putBoolean("is_veteran", true)
            .putString("some_str", "some some")
            .putInt("age", 24)
            .putFloat("pie", 3f)
            .putLong("amount", 12L)
            .putStringSet("str_list", setOf("1", "2", "3"))
            .putString("some_class_list", "0,1,2")
            .commit()

        repository.store(null)

        assertThat(preferences.contains("is_veteran")).isFalse()
        assertThat(preferences.contains("some_str")).isFalse()
        assertThat(preferences.contains("age")).isFalse()
        assertThat(preferences.contains("pie")).isFalse()
        assertThat(preferences.contains("amount")).isFalse()
        assertThat(preferences.contains("str_list")).isFalse()
        assertThat(preferences.contains("some_class_list")).isFalse()
    }
}
