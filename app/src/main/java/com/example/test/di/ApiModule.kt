package com.example.test.di

import com.example.test.api.DemoApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ApiModule {

    @Provides
    @Singleton
    fun provideDemoApi(
        retrofit: Retrofit
    ): DemoApi {
        return retrofit.create(DemoApi::class.java)
    }
}