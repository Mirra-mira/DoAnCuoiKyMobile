package com.example.doancuoikymobile.utils

object Logger {
    var enable = true

    fun d(tag: String, msg: String) {
        if (enable) {
            println("$tag: $msg")
        }
    }
}
