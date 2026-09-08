package com.hacybeyker.scaffoldingandroidcompose.feature.home.data

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class InMemoryGreetingRepositoryTest {

    private val repository = InMemoryGreetingRepository()

    @Test
    fun `starts with the default name`() = runTest {
        repository.observeGreeting().test {
            assertEquals("Android", awaitItem().name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits the updated name to active collectors`() = runTest {
        repository.observeGreeting().test {
            assertEquals("Android", awaitItem().name)

            repository.updateName("Kotlin")

            assertEquals("Kotlin", awaitItem().name)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
