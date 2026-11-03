package com.hacybeyker.scaffoldingandroidcompose.feature.home.domain.repository

import com.hacybeyker.scaffoldingandroidcompose.feature.home.domain.model.Greeting
import kotlinx.coroutines.flow.Flow

interface GreetingRepository {
    fun observeGreeting(): Flow<Greeting>

    suspend fun updateName(name: String)
}
