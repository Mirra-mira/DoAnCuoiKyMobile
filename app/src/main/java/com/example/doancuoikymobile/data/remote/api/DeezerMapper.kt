package com.example.doancuoikymobile.data.remote.api

import com.example.doancuoikymobile.model.Artist
import com.example.doancuoikymobile.model.Song

/**
 * Mapper for converting Deezer API responses to domain models
 *
 * IMPORTANT: Song.audioUrl is reserved for FULL MP3 (user uploaded or full track)
 * Deezer preview (~30s) maps to Song.previewUrl instead
 *
 * Mapping rules:
 * - Deezer preview URL → Song.previewUrl (30s preview, NEVER overwrite audioUrl)
 * - Album cover → Song.coverUrl
 * - Deezer ID → Song.songId (as String)
 * - Artist name → mainArtistId (simplified for now)
 */

/**
 * Map DeezerTrack to Song
 *
 * Priority when playing:
 * 1. Song.audioUrl (full MP3 if available)
 * 2. Song.previewUrl (Deezer 30s preview fallback)
 *
 * @return Song entity with filled fields (never overwrites audioUrl)
 */
fun DeezerTrack.toSong(): Song {
    return Song(
        songId = this.id.toString(),
        title = this.title,
        duration = this.duration, // Already in seconds
        audioUrl = "", // NEVER fill from Deezer - reserved for full MP3
        previewUrl = this.preview, // Deezer preview URL (30s MP3)
        coverUrl = this.album?.cover_big ?: this.album?.cover, // Use bigger cover if available
        mainArtistId = this.artist?.id.toString(), // Store artist ID as string
        artistName = this.artist?.name, // Artist name for display
        isOnline = true // This is from online API
    )
}

/**
 * Map DeezerTrackResponse to Song (used for single track fetch)
 * 
 * IMPORTANT: Never fills audioUrl - that's for full track files only
 */
fun DeezerTrackResponse.toSong(): Song {
    return Song(
        songId = this.id.toString(),
        title = this.title,
        duration = this.duration,
        audioUrl = "", // NEVER fill from Deezer
        previewUrl = this.preview, // Deezer preview only
        coverUrl = this.album?.cover_big ?: this.album?.cover,
        mainArtistId = this.artist?.id.toString(),
        artistName = this.artist?.name, // Artist name for display
        isOnline = true
    )
}

/**
 * Map DeezerArtist to Artist
 */
fun DeezerArtist.toArtist(): Artist {
    return Artist(
        artistId = this.id.toString(),
        name = this.name,
        pictureUrl = this.picture_big ?: this.picture, // Use bigger picture if available
        searchKeywords = this.name.split(" ") // Simple keyword extraction
    )
}

/**
 * Map DeezerArtistResponse to Artist (used for single artist fetch)
 */
fun DeezerArtistResponse.toArtist(): Artist {
    return Artist(
        artistId = this.id.toString(),
        name = this.name,
        pictureUrl = this.picture_big ?: this.picture,
        searchKeywords = this.name.split(" ")
    )
}

/**
 * Extract artist from track and convert to Artist entity
 */
fun DeezerTrack.extractArtist(): Artist? {
    return this.artist?.toArtist()
}

/**
 * Helper: Sanitize Deezer cover URL if needed
 * Deezer returns HTTPS URLs, but sometimes need to handle redirects
 */
fun String.sanitizeCoverUrl(): String {
    return if (this.isEmpty()) "" else this
}
