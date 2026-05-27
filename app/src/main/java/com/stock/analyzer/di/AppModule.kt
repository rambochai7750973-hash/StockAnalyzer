package com.stock.analyzer.di

import android.content.Context
import androidx.room.Room
import com.stock.analyzer.data.local.AppDatabase
import com.stock.analyzer.data.local.dao.StockDao
import com.stock.analyzer.data.local.dao.WatchlistDao
import com.stock.analyzer.data.remote.SinaApi
import com.stock.analyzer.data.remote.TencentApi
import com.stock.analyzer.domain.repository.StockRepository
import com.stock.analyzer.domain.repository.WatchlistRepository
import com.stock.analyzer.data.repository.StockRepositoryImpl
import com.stock.analyzer.data.repository.WatchlistRepositoryImpl
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SinaRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class TencentRetrofit

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().create()

    @Provides
    @Singleton
    fun provideOkHttp(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Referer", "https://finance.sina.com.cn")
                    .build()
                chain.proceed(request)
            }
            .build()
    }

    @SinaRetrofit
    @Provides
    @Singleton
    fun provideSinaRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://hq.sinajs.cn/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @TencentRetrofit
    @Provides
    @Singleton
    fun provideTencentRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://qt.gtimg.cn/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideSinaApi(@SinaRetrofit retrofit: Retrofit): SinaApi {
        return retrofit.create(SinaApi::class.java)
    }

    @Provides
    @Singleton
    fun provideTencentApi(@TencentRetrofit retrofit: Retrofit): TencentApi {
        return retrofit.create(TencentApi::class.java)
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "stock_analyzer.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideStockDao(db: AppDatabase): StockDao = db.stockDao()

    @Provides
    @Singleton
    fun provideWatchlistDao(db: AppDatabase): WatchlistDao = db.watchlistDao()

    @Provides
    @Singleton
    fun provideStockRepository(impl: StockRepositoryImpl): StockRepository = impl

    @Provides
    @Singleton
    fun provideWatchlistRepository(impl: WatchlistRepositoryImpl): WatchlistRepository = impl
}
