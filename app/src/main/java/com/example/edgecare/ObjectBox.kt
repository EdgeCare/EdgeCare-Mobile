package com.example.edgecare

import android.content.Context
import android.util.Log
import io.objectbox.BoxStore
import io.objectbox.android.Admin


object ObjectBox {
    lateinit var store: BoxStore
        private set

    fun init(context: Context) {
        store = MyObjectBox.builder()
            .androidContext(context)
            .build()

    }
}