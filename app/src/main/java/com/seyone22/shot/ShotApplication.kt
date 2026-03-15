package com.seyone22.shot

import android.app.Application
import com.seyone22.shot.di.AppContainer
import com.seyone22.shot.di.DefaultAppContainer

class ShotApplication : Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}