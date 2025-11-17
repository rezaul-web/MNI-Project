package com.example.skincancerdetectormni

import com.example.skincancerdetectormni.network.MniApi
import com.example.skincancerdetectormni.network.NetworkModule
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import okhttp3.mockwebserver.MockWebServer
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [NetworkModule::class]
)
object NetworkTestModule {

    @Provides
    @Singleton
    fun provideMockWebServer(): MockWebServer {
        return MockWebServer()
    }

    @Provides
    @Singleton
    fun provideRetrofit(mockWebServer: MockWebServer): Retrofit {
        return Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): MniApi {
        return retrofit.create(MniApi::class.java)
    }
}