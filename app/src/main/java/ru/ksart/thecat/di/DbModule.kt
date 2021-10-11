package ru.ksart.thecat.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.ksart.thecat.model.db.CatDao
import ru.ksart.thecat.model.db.CatDb
import ru.ksart.thecat.model.db.CatDbImpl
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DbModule {

    @CatRoomDb
    @Provides
    @Singleton
    fun provideCatDb(@ApplicationContext context: Context): CatDb {
        return Room.databaseBuilder(
            context,
            CatDbImpl::class.java,
            CatDb.DB_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideCatDao(@CatRoomDb roomDb: CatDb): CatDao = roomDb.getCatDao()
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CatRoomDb
