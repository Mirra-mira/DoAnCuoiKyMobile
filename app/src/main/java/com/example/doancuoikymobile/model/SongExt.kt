package com.example.doancuoikymobile.model

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.*

/**
 * Extension function to convert Song model to MediaMetadataCompat
 * Used for media controls & playback
 */
fun Song.toMediaMetadata(): MediaMetadataCompat {
    return MediaMetadataCompat.Builder()
        .putString(METADATA_KEY_ARTIST, "Unknown Artist") // TODO: Get actual artist name
        .putString(METADATA_KEY_MEDIA_ID, this.songId)
        .putString(METADATA_KEY_TITLE, this.title)
        .putString(METADATA_KEY_DISPLAY_TITLE, this.title)
        .putString(METADATA_KEY_DISPLAY_ICON_URI, this.coverUrl)
        .putString(METADATA_KEY_MEDIA_URI, this.audioUrl)
        .putString(METADATA_KEY_ALBUM_ART_URI, this.coverUrl)
        .putString(METADATA_KEY_DISPLAY_SUBTITLE, "Unknown Artist")
        .putString(METADATA_KEY_DISPLAY_DESCRIPTION, "Unknown Artist")
        .putLong(METADATA_KEY_DURATION, this.duration.toLong() * 1000) // Convert to milliseconds
        .build()
}

/**
 * Extension function to convert MediaMetadataCompat back to Song model
 */
fun MediaMetadataCompat.toSong(): Song? {
    return description?.let {
        Song(
            songId = it.mediaId ?: "",
            title = it.title.toString(),
            duration = (getLong(METADATA_KEY_DURATION) / 1000).toInt(),
            audioUrl = it.mediaUri.toString(),
            coverUrl = it.iconUri.toString()
        )
    }
}
