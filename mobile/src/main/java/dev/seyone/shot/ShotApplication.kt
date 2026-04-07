package dev.seyone.shot

import android.app.Application
import dev.seyone.shot.di.AppContainer
import dev.seyone.shot.di.DefaultAppContainer

class ShotApplication : Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}