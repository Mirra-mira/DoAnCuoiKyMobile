package com.example.doancuoikymobile.repository

import com.example.doancuoikymobile.data.remote.firebase.SongRemoteDataSource
import com.example.doancuoikymobile.data.remote.api.DeezerRetrofitClient
import com.example.doancuoikymobile.data.remote.api.toSong
import com.example.doancuoikymobile.data.remote.api.DeezerApiService
import com.example.doancuoikymobile.model.Song
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * SongRepository
 *
 * Điều phối dữ liệu từ:
 * - Firebase Firestore: Thư viện cá nhân
 * - Deezer API: Tìm kiếm và khám phá nhạc mới
 */
class SongRepository(
    private val songRemoteDataSource: SongRemoteDataSource = SongRemoteDataSource(),
    private val deezerApi: DeezerApiService = DeezerRetrofitClient.deezerApiService
) {

    /**
     * Lấy toàn bộ bài hát từ Firestore (Real-time stream)
     */
    fun getAllSongs(): Flow<List<Song>> {
        return songRemoteDataSource.watchAllSongs()
    }

    /**
     * Lấy 1 bài hát theo ID từ Firestore
     */
    suspend fun getSongById(songId: String): Song? {
        return songRemoteDataSource.getSongOnce(songId)
    }

    /**
     * Tìm kiếm bài hát từ Deezer API
     * Sửa lỗi: Check CancellationException để tránh AbortFlowException
     */
    fun searchSongs(query: String): Flow<Resource<List<Song>>> = flow {
        emit(Resource.loading(null))
        try {
            val response = deezerApi.searchTracks(query)
            val songs = response.data.map { it.toSong() }
            emit(Resource.success(songs))
        } catch (e: Exception) {
            // Nếu lỗi là do Coroutine bị hủy (người dùng thoát màn hình/dừng test)
            // thì phải ném lỗi đó đi để Flow đóng lại một cách minh bạch.
            if (e is CancellationException) throw e
            emit(Resource.error("Error searching songs: ${e.message}", null))
        }
    }

    /**
     * Lấy chi tiết bài hát từ Deezer API
     * Sửa lỗi: Loại bỏ return@flow và thêm kiểm tra CancellationException
     */
    fun getSongDetailFromDeezer(id: Long): Flow<Resource<Song>> = flow {
        emit(Resource.loading(null))
        try {
            val response = deezerApi.getTrack(id)
            val song = response.toSong()
            emit(Resource.success(song))
        } catch (e: Exception) {
            // Tương tự, tránh emit thêm dữ liệu khi Flow đã bị hủy (aborted)
            if (e is CancellationException) throw e
            emit(Resource.error("Error fetching song details: ${e.message}", null))
        }
    }

    /**
     * Lưu bài hát vào Firestore
     */
    suspend fun saveSong(song: Song): Boolean {
        return songRemoteDataSource.saveSong(song)
    }

    /**
     * Xóa bài hát khỏi Firestore
     */
    suspend fun deleteSong(songId: String): Boolean {
        return songRemoteDataSource.deleteSong(songId)
    }
}