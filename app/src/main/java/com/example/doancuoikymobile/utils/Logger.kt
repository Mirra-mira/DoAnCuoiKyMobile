package com.example.doancuoikymobile.utils

object Logger {

    fun d(tag: String, message: String) {
        android.util.Log.d(tag, message)
    }

    fun e(tag: String, throwable: Throwable) {
        android.util.Log.e(tag, throwable.message, throwable)
    }

    fun w(tag: String, message: String) {
        android.util.Log.w(tag, message)
    }
}
