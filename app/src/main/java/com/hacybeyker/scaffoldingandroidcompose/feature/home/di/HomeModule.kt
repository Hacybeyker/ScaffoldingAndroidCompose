package com.hacybeyker.scaffoldingandroidcompose.feature.home.di

import com.hacybeyker.scaffoldingandroidcompose.feature.home.data.repository.InMemoryGreetingRepository
import com.hacybeyker.scaffoldingandroidcompose.feature.home.domain.repository.GreetingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class HomeModule {

    @Binds
    abstract fun bindGreetingRepository(impl: InMemoryGreetingRepository): GreetingRepository
}
