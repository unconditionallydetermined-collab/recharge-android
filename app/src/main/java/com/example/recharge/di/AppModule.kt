package com.example.recharge.di

import android.content.Context
import androidx.room.Room
import com.example.recharge.BuildConfig
import com.example.recharge.data.room.RechargeDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): RechargeDatabase =
        Room.databaseBuilder(context, RechargeDatabase::class.java, "recharge.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideQueueDao(db: RechargeDatabase) = db.queueItemDao()
    @Provides fun provideSessionDao(db: RechargeDatabase) = db.sessionDao()
    @Provides fun provideEventDao(db: RechargeDatabase) = db.eventDao()

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL.ifEmpty { "https://placeholder.supabase.co" },
        supabaseAnonKey = BuildConfig.SUPABASE_ANON_KEY.ifEmpty { "placeholder" }
    ) {
        install(Auth)
        install(Postgrest)
    }
}
