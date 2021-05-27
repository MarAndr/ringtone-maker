package com.example.ringtonemaker

import android.app.Application
import android.util.Log
import timber.log.Timber

class ControlApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG){
        Timber.plant(Timber.DebugTree())
        }
    }
}