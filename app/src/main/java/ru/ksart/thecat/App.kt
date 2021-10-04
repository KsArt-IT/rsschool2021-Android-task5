package ru.ksart.thecat

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        // инициализируем Timber
        Timber.plant(Timber.DebugTree())
    }
}
