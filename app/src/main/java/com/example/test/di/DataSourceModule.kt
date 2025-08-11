package com.example.test.di

import android.content.Context
import com.example.test.api.DemoApi
import com.example.test.model.datasource.HoldingsLocalDataSource
import com.example.test.model.datasource.HoldingsLocalDataSourceImpl
import com.example.test.model.datasource.HoldingsRemoteDataSource
import com.example.test.model.datasource.HoldingsRemoteDataSourceImpl
import com.example.test.model.local.AppDatabase
import com.example.test.model.local.HoldingsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DataSourceModule {

    @Provides
    @Singleton
    fun provideHoldingsRemoteDataSource(
        demoApi: DemoApi
    ): HoldingsRemoteDataSource {
        return HoldingsRemoteDataSourceImpl(
            demoApi = demoApi
        )
    }
    
    @Provides
    @Singleton
    fun provideHoldingsLocalDataSource(
        holdingsDao: HoldingsDao
    ): HoldingsLocalDataSource {
        return HoldingsLocalDataSourceImpl(
            holdingsDao = holdingsDao
        )
    }
    
    @Provides
    @Singleton
    fun provideHoldingsDao(
        database: AppDatabase
    ): HoldingsDao {
        return database.holdingsDao()
    }
    
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return AppDatabase.getDatabase(context)
    }
}