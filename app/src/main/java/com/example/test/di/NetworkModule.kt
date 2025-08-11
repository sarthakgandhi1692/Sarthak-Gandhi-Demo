package com.example.test.di

import com.example.test.api.StringConverter
import com.example.test.di.qualifiers.BaseUrl
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Provides
    @Singleton
    fun providesMoshiConverterFactory(
        moshi: Moshi
    ): MoshiConverterFactory {
        return MoshiConverterFactory.create(moshi)
    }


    @Provides
    @Singleton
    fun providesMoshiBuilder(): Moshi.Builder {
        return Moshi.Builder()
    }

    @Provides
    @Singleton
    fun providesMoshiInstance(
        moshiBuilder: Moshi.Builder
    ): Moshi {
        return moshiBuilder.build()
    }

    @Provides
    @Singleton
    fun getOkhttpClient(
    ): OkHttpClient {
        return OkHttpClient().newBuilder()
            .connectTimeout(15000, TimeUnit.MILLISECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .apply {
                val logging = HttpLoggingInterceptor()
                logging.setLevel(HttpLoggingInterceptor.Level.BODY)
                addInterceptor(logging)
            }
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        @BaseUrl baseUrl: String,
        okHttpClient: OkHttpClient,
        coroutineCallAdapterFactory: CoroutineCallAdapterFactory,
        stringConverter: StringConverter,
        scalarsConverterFactory: ScalarsConverterFactory,
        moshiConverterFactory: MoshiConverterFactory,
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(scalarsConverterFactory)
            .addConverterFactory(stringConverter)
            .addConverterFactory(moshiConverterFactory)
            .addCallAdapterFactory(coroutineCallAdapterFactory)
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    fun getCoroutineCallerAdapterFactory(): CoroutineCallAdapterFactory {
        return CoroutineCallAdapterFactory()
    }

    @Provides
    @Singleton
    fun getStringConverter(): StringConverter {
        return StringConverter()
    }

    @Provides
    @Singleton
    fun getScalarsConverterFactory(): ScalarsConverterFactory {
        return ScalarsConverterFactory.create()
    }
}