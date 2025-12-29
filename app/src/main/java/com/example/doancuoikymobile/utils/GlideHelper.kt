package com.example.doancuoikymobile.utils

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions

/**
 * Glide Image Loading Setup
 * Follows pattern from project reference for consistent image handling
 */
object GlideHelper {

    fun getGlideRequestOptions(context: Context) = RequestOptions()
        .placeholder(android.R.drawable.ic_menu_gallery)
        .error(android.R.drawable.ic_menu_gallery)
        .diskCacheStrategy(DiskCacheStrategy.DATA)

    fun getGlideInstance(context: Context) =
        Glide.with(context).setDefaultRequestOptions(getGlideRequestOptions(context))
}
