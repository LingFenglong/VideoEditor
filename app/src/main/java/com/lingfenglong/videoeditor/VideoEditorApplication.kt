package com.lingfenglong.videoeditor

import android.app.Application
import android.content.Context

class VideoEditorApplication() : Application() {
    companion object {
        lateinit var application: Context
    }

    override fun onCreate() {
        super.onCreate()
        application = applicationContext
    }
}
