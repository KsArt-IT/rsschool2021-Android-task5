package ru.ksart.thecat.utils

import android.util.Log
import ru.ksart.thecat.BuildConfig

object DebugHelper {
    private const val DEBUG = "cat145"

    fun log(msg: String) {
        if (BuildConfig.DEBUG) Log.d(DEBUG, msg)
    }

    fun log(msg: String, tr: Throwable) {
        if (BuildConfig.DEBUG) Log.e(DEBUG, msg, tr)
    }
}
