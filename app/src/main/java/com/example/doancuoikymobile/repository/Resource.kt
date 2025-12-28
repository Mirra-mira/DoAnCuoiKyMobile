package com.example.doancuoikymobile.repository

/**
 * A generic class that holds a value with its loading status.
 *
 * Status can be SUCCESS, LOADING, or ERROR
 *
 * Inspired by Google's Architecture Blueprints
 */
data class Resource<out T>(
    val status: Status,
    val data: T?,
    val message: String?
) {
    companion object {
        fun <T> success(data: T?): Resource<T> =
            Resource(Status.SUCCESS, data, null)

        fun <T> error(message: String, data: T?): Resource<T> =
            Resource(Status.ERROR, data, message)

        fun <T> loading(data: T?): Resource<T> =
            Resource(Status.LOADING, data, null)
    }
}

enum class Status {
    SUCCESS,
    ERROR,
    LOADING
}
