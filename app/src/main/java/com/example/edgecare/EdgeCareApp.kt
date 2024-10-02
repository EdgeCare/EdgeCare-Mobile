package com.example.edgecare

import android.app.Application
import android.util.Log
import io.objectbox.android.Admin

class EdgeCareApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ObjectBox.init(this)
    }
}