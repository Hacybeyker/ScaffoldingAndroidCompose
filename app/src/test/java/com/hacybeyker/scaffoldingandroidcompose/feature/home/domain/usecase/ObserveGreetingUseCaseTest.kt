package com.hacybeyker.scaffoldingandroidcompose.feature.home.domain.usecase

import app.cash.turbine.test
import com.hacybeyker.scaffoldingandroidcompose.feature.home.FakeGreetingRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveGreetingUseCaseTest {

    private val repository = FakeGreetingRepository(initialName = "Android")
    private val useCase = ObserveGreetingUseCase(repository)

    @Test
    fun `emits the current greeting`() = runTest {
        useCase().test {
            assertEquals("Android", awaitItem().name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `re-emits when the name changes`() = runTest {
        useCase().test {
            assertEquals("Android", awaitItem().name)

            repository.updateName("Compose")

            assertEquals("Compose", awaitItem().name)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
