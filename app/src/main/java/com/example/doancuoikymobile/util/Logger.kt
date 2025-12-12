package com.example.doancuoikymobile.util

object Logger {
    var enable = true

    fun d(tag: String, msg: String) {
        if (enable) {
            println("$tag: $msg")
        }
    }
}
