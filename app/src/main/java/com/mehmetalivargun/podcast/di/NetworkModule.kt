package com.mehmetalivargun.podcast.di

import android.content.Context
import com.mehmetalivargun.podcast.data.PodcastMediaSource
import com.mehmetalivargun.podcast.data.service.MediaPlayerServiceConnection
import com.mehmetalivargun.podcast.remote.ITunesService
import com.mehmetalivargun.podcast.remote.RssService
import com.mehmetalivargun.podcast.repository.ITunesRepo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Singleton
    @Provides
    fun provideRssService(): RssService {
        return RssService()
    }
    @Singleton
    @Provides
    fun provideRepo(): ITunesRepo {
        return ITunesRepo(provideService())
    }

    @Singleton
    @Provides
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        return interceptor
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(provideHttpLoggingInterceptor())
            .build()
    }

    @Singleton
    @Provides
    fun provideService(): ITunesService {
        return Retrofit.Builder()
            .baseUrl("https://itunes.apple.com")
            .client(provideOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ITunesService::class.java)
    }
    @Provides
    @Singleton
    fun provideMediaPlayerServiceConnection(
        @ApplicationContext context: Context,
        mediaSource: PodcastMediaSource
    ): MediaPlayerServiceConnection = MediaPlayerServiceConnection(context, mediaSource)

}
