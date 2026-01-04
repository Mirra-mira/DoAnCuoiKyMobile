package com.example.doancuoikymobile.repository

import com.example.doancuoikymobile.data.remote.firebase.SongRemoteDataSource
import com.example.doancuoikymobile.data.remote.api.DeezerRetrofitClient
import com.example.doancuoikymobile.data.remote.api.toSong
import com.example.doancuoikymobile.data.remote.api.DeezerApiService
import com.example.doancuoikymobile.model.Song
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import com.example.doancuoikymobile.ui.home.ContentCard
import com.example.doancuoikymobile.ui.home.ContentType

class SongRepository(
    private val songRemoteDataSource: SongRemoteDataSource = SongRemoteDataSource(),
    private val deezerApi: DeezerApiService = DeezerRetrofitClient.deezerApiService) {

    fun getAllSongs(): Flow<List<Song>> {
        return songRemoteDataSource.watchAllSongs()
    }

    suspend fun getSongById(songId: String): Song? {
        // 1. Thử lấy từ Firebase
        val songFromFirebase = songRemoteDataSource.getSongOnce(songId)

        // Nếu có bài hát và có link nhạc thì trả về luôn
        if (songFromFirebase != null && !songFromFirebase.previewUrl.isNullOrEmpty()) {
            return songFromFirebase
        }

        // 2. Nếu không có link, "cứu" bằng cách gọi Deezer API (vì songId chính là Deezer ID)
        return try {
            val response = deezerApi.getTrack(songId.toLong())
            response.toSong() // Mapper sẽ điền previewUrl vào đây
        } catch (e: Exception) {
            songFromFirebase // Trả về bản gốc nếu API lỗi
        }
    }

    fun searchSongs(query: String): Flow<Resource<List<Song>>> = flow {
        emit(Resource.loading(null))
        try {
            val firebaseSongs = songRemoteDataSource.searchSongsOnce(query)
            if (firebaseSongs.isNotEmpty()) {
                emit(Resource.success(firebaseSongs))
            } else {
                val response = deezerApi.searchTracks(query)
                val songs = response.data.map { it.toSong() }
                emit(Resource.success(songs))
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(Resource.error("Error searching songs: ${e.message}", null))
        }
    }

    fun getSongDetailFromDeezer(id: Long): Flow<Resource<Song>> = flow {
        emit(Resource.loading(null))
        try {
            val response = deezerApi.getTrack(id)
            val song = response.toSong()
            emit(Resource.success(song))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emit(Resource.error("Error fetching song details: ${e.message}", null))
        }
    }

    suspend fun saveSong(song: Song): Boolean {
        return songRemoteDataSource.saveSong(song)
    }

    suspend fun saveSongIfNotExists(song: Song): Boolean {
        return songRemoteDataSource.saveSongIfNotExists(song)
    }

    suspend fun deleteSong(songId: String): Boolean {
        return songRemoteDataSource.deleteSong(songId)
    }

    suspend fun getDeezerGenres(): List<com.example.doancuoikymobile.ui.home.Genre> {
        return try {
            listOf(
                com.example.doancuoikymobile.ui.home.Genre("1313621735", "Top Viet Nam"),
                com.example.doancuoikymobile.ui.home.Genre("3155776842", "Top Global"),
                com.example.doancuoikymobile.ui.home.Genre("1116182241", "Top US-UK"),
                com.example.doancuoikymobile.ui.home.Genre("19652321", "Rap Viet"),
                com.example.doancuoikymobile.ui.home.Genre("12250117075", "V-Pop Hot"),
                com.example.doancuoikymobile.ui.home.Genre("53362031", "EDM Hits")
            )
        } catch (e: Exception) { emptyList() }
    }

    suspend fun getNewReleasesFromDeezer(): List<Song> {
        return try {
            val response = deezerApi.searchTracks("latest 2024", 20)
            response.data.map { it.toSong() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getTopPlaylists(): List<ContentCard> {
        return try {
            val response = deezerApi.searchPlaylists("Popular", 10)
            response.data.map {
                ContentCard(
                    id = it.id.toString(),
                    title = it.title,
                    subtitle = "Deezer Playlist",
                    imageUrl = it.picture_big ?: it.picture ?: "",
                    type = ContentType.PLAYLIST
                )
            }
        } catch (e: Exception) { emptyList() }
    }

    suspend fun getPlaylistTracks(playlistId: Long): List<Song> {
        val response = deezerApi.getPlaylistTracks(playlistId, limit = 100)
        return response.data.map { it.toSong() }
    }

    suspend fun getDeezerPlaylistDetails(playlistId: Long) =
        deezerApi.getPlaylist(playlistId)

}