package com.example.edgecare

import android.content.Context
import com.example.edgecare.models.MyObjectBox
import io.objectbox.BoxStore


object ObjectBox {
    lateinit var store: BoxStore
        private set

    fun init(context: Context) {
        store = MyObjectBox.builder()
            .androidContext(context)
            .build()

    }
}