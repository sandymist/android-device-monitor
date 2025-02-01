package com.sandymist.android.devicemonitor

import android.app.Application
import timber.log.Timber

class DemoApp: Application() {
    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())
    }
}
