package ru.ksart.thecat.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import ru.ksart.thecat.model.repositories.CatRepository
import ru.ksart.thecat.model.repositories.CatRepositoryImpl

@Module
@InstallIn(ViewModelComponent::class)
abstract class RepositoryModule {

    @Binds
    @ViewModelScoped
    abstract fun provideCatRepository(repositoryImpl: CatRepositoryImpl): CatRepository
}
