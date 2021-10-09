package ru.ksart.thecat.di

import androidx.lifecycle.SavedStateHandle
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped

@Module
@InstallIn(ActivityComponent::class)
class HandlerModule {

    @ActivityScoped
    @Provides
    fun provideHandler() = SavedStateHandle()
}
