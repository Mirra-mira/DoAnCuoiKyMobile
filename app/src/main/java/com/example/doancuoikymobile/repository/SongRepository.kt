package com.example.doancuoikymobile.repository

import com.example.doancuoikymobile.data.remote.firebase.SongRemoteDataSource
import com.example.doancuoikymobile.data.remote.api.DeezerRetrofitClient
import com.example.doancuoikymobile.data.remote.api.toSong
import com.example.doancuoikymobile.data.remote.api.DeezerApiService
import com.example.doancuoikymobile.model.Song
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SongRepository(
    private val songRemoteDataSource: SongRemoteDataSource = SongRemoteDataSource(),
    private val deezerApi: DeezerApiService = DeezerRetrofitClient.deezerApiService
) {

    fun getAllSongs(): Flow<List<Song>> {
        return songRemoteDataSource.watchAllSongs()
    }

    suspend fun getSongById(songId: String): Song? {
        return songRemoteDataSource.getSongOnce(songId)
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
            val response = deezerApi.getGenres()
            // Lọc bỏ genre "All" thường có ID là 0
            response.data.filter { it.id != "0" }.map {
                com.example.doancuoikymobile.ui.home.Genre(it.id, it.name)
            }
        } catch (e: Exception) { emptyList() }
    }

    suspend fun getNewReleasesFromDeezer(): List<Song> {
        return try {
            // Lấy album mới, sau đó lấy track đầu tiên hoặc map album thành "Song" giả lập
            // Cách đơn giản nhất cho Home: Lấy nhạc đang trending (Chart) vì nó có sẵn track
            val response = deezerApi.searchTracks("top", 20)
            response.data.map { it.toSong() }
        } catch (e: Exception) { emptyList() }
    }
}