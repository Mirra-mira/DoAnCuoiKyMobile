package com.example.doancuoikymobile.data.remote.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Deezer API Service - PUBLIC API (no OAuth required)
 * Base URL: https://api.deezer.com
 *
 * Endpoints used:
 * - search/track: Search tracks by keyword
 * - track/{id}: Get track details
 * - artist/{id}: Get artist details
 */
interface DeezerApiService {
    
    /**
     * Search tracks by query
     * @param q Search keyword
     * @param limit Number of results (default: 30)
     */
    @GET("search/track")
    suspend fun searchTracks(
        @Query("q") q: String,
        @Query("limit") limit: Int = 30
    ): DeezerSearchResponse

    /**
     * Get track details by ID
     * @param id Track ID
     */
    @GET("track/{id}")
    suspend fun getTrack(
        @Path("id") id: Long
    ): DeezerTrackResponse

    @GET("search/artist")
    suspend fun searchArtists(
        @Query("q") q: String,
        @Query("limit") limit: Int = 30
    ): DeezerArtistSearchResponse

    /**
     * Search playlists by query
     * @param q Search keyword
     * @param limit Number of results (default: 30)
     */
    @GET("search/playlist")
    suspend fun searchPlaylists(
        @Query("q") q: String,
        @Query("limit") limit: Int = 30
    ): DeezerPlaylistSearchResponse

    /**
     * Get artist details by ID
     * @param id Artist ID
     */
    @GET("artist/{id}")
    suspend fun getArtist(
        @Path("id") id: Long
    ): DeezerArtistResponse

    /**
     * Get top tracks of an artist
     * @param id Artist ID
     * @param limit Number of results (default: 50)
     */
    @GET("artist/{id}/top")
    suspend fun getArtistTopTracks(
        @Path("id") id: Long,
        @Query("limit") limit: Int = 50
    ): DeezerSearchResponse

    /**
     * Get playlist details by ID
     * @param id Playlist ID
     */
    @GET("playlist/{id}")
    suspend fun getPlaylist(
        @Path("id") id: Long
    ): DeezerPlaylistResponse

    /**
     * Get tracks of a playlist
     * @param id Playlist ID
     * @param limit Number of results (default: 100)
     */
    @GET("playlist/{id}/tracks")
    suspend fun getPlaylistTracks(
        @Path("id") id: Long,
        @Query("limit") limit: Int = 100
    ): DeezerSearchResponse
}

// ==================== RESPONSE DTOs ====================

/**
 * Response for search/track endpoint
 */
data class DeezerSearchResponse(
    val data: List<DeezerTrack> = emptyList(),
    val total: Int = 0,
    val next: String? = null
)

/**
 * Track object from Deezer API
 * Contains preview URL (~30s MP3), artist, and album info
 */
data class DeezerTrack(
    val id: Long = 0,
    val title: String = "",
    val duration: Int = 0, // in seconds
    val preview: String? = null, // 30s preview MP3 URL
    val artist: DeezerArtist? = null,
    val album: DeezerAlbum? = null,
    val rank: Int = 0, // Deezer rank (popularity indicator)
    val explicit_lyrics: Boolean = false
)

/**
 * Single track response (when fetching by ID)
 */
data class DeezerTrackResponse(
    val id: Long = 0,
    val title: String = "",
    val duration: Int = 0,
    val preview: String? = null,
    val artist: DeezerArtist? = null,
    val album: DeezerAlbum? = null,
    val rank: Int = 0,
    val explicit_lyrics: Boolean = false
)

/**
 * Artist object from Deezer API
 */
data class DeezerArtist(
    val id: Long = 0,
    val name: String = "",
    val picture: String? = null, // Artist picture URL
    val picture_big: String? = null,
    val nb_album: Int = 0,
    val nb_fan: Int = 0
)

data class DeezerArtistSearchResponse(
    val data: List<DeezerArtist> = emptyList(),
    val total: Int = 0,
    val next: String? = null
)

/**
 * Single artist response (when fetching by ID)
 */
data class DeezerArtistResponse(
    val id: Long = 0,
    val name: String = "",
    val picture: String? = null,
    val picture_big: String? = null,
    val nb_album: Int = 0,
    val nb_fan: Int = 0
)

/**
 * Album object from Deezer API
 */
data class DeezerAlbum(
    val id: Long = 0,
    val title: String = "",
    val cover: String? = null, // Album cover URL
    val cover_big: String? = null,
    val release_date: String? = null
)

/**
 * Playlist object from Deezer API
 */
data class DeezerPlaylist(
    val id: Long = 0,
    val title: String = "",
    val description: String? = null,
    val picture: String? = null, // Playlist picture URL
    val picture_big: String? = null,
    val nb_tracks: Int = 0,
    val creator: DeezerUser? = null
)

/**
 * User object from Deezer API (playlist creator)
 */
data class DeezerUser(
    val id: Long = 0,
    val name: String = ""
)

/**
 * Response for search/playlist endpoint
 */
data class DeezerPlaylistSearchResponse(
    val data: List<DeezerPlaylist> = emptyList(),
    val total: Int = 0,
    val next: String? = null
)

/**
 * Single playlist response (when fetching by ID)
 */
data class DeezerPlaylistResponse(
    val id: Long = 0,
    val title: String = "",
    val description: String? = null,
    val picture: String? = null,
    val picture_big: String? = null,
    val nb_tracks: Int = 0,
    val creator: DeezerUser? = null
)
