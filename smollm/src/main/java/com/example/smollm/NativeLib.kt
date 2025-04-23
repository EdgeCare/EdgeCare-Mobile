package com.example.smollm

class NativeLib {

    /**
     * A native method that is implemented by the 'smollm' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {
        // Used to load the 'smollm' library on application startup.
        init {
            System.loadLibrary("smollm")
        }
    }
}