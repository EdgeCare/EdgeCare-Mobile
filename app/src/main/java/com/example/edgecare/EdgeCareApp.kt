package com.example.edgecare

import android.app.Application
import com.example.edgecare.utils.EmbeddingUtils

class EdgeCareApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ObjectBox.init(this)
        EmbeddingUtils.initializeModel(this)
    }
}