package com.example.test.di

import com.example.test.model.datasource.HoldingsLocalDataSource
import com.example.test.model.datasource.HoldingsRemoteDataSource
import com.example.test.model.repository.HoldingsRepository
import com.example.test.model.repository.HoldingsRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {

    @Provides
    @Singleton
    fun provideHoldingsRepository(
        holdingsRemoteDataSource: HoldingsRemoteDataSource,
        holdingsLocalDataSource: HoldingsLocalDataSource
    ): HoldingsRepository {
        return HoldingsRepositoryImpl(
            holdingsRemoteDataSource = holdingsRemoteDataSource,
            holdingsLocalDataSource = holdingsLocalDataSource
        )
    }
}