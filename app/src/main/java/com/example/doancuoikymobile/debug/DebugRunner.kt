package com.example.doancuoikymobile.debug

import android.util.Log
import com.example.doancuoikymobile.data.remote.api.*
import com.example.doancuoikymobile.data.remote.firebase.*
import com.example.doancuoikymobile.model.*
import com.example.doancuoikymobile.repository.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

/**
 * Object dùng để chạy thử nghiệm các tính năng của Repository và API.
 * Kết quả in ra Logcat với thẻ "DEBUG_RUNNER".
 */
object DebugRunner {

    private const val TAG = "DEBUG_RUNNER"

    fun runAll() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "🚀 STARTING ALL SYSTEM TESTS...")

                // 1. Test Tầng API & Mapper (Mới)
                runTestSafely("Deezer API & Mappers") { testDeezerApiAndMappers() }

                // 2. Test Các Repository
                runTestSafely("Auth Repository") { testAuthRepository() }
                runTestSafely("User Repository") { testUserRepository() }
                runTestSafely("Song Repository") { testSongRepository() }
                runTestSafely("Artist Repository") { testArtistRepository() }
                runTestSafely("Playlist Repository") { testPlaylistRepository() }
                runTestSafely("Favorite Repository") { testFavoriteRepository() }
                runTestSafely("Recently Played") { testRecentlyPlayed() }

                Log.d(TAG, "✅ ALL TESTS COMPLETED SUCCESSFULLY")
            } catch (e: Exception) {
                Log.e(TAG, "❌ CRITICAL ERROR DURING TEST RUNNER: ${e.message}")
            }
        }
    }

    /**
     * Helper để chạy từng bài test mà không làm sập cả hệ thống nếu 1 cái fail
     */
    private suspend fun runTestSafely(name: String, block: suspend () -> Unit) {
        try {
            Log.d(TAG, "▶ Testing: $name...")
            block()
        } catch (e: Exception) {
            Log.e(TAG, "❌ $name FAILED: ${e.localizedMessage}")
        }
    }

    /**
     * TEST MỚI: Kiểm tra trực tiếp Retrofit Client và logic Mapping dữ liệu
     */
    private suspend fun testDeezerApiAndMappers() {
        val apiService = DeezerRetrofitClient.deezerApiService

        // Gọi API Search thật
        val searchResponse = apiService.searchTracks("Sơn Tùng M-TP", limit = 1)
        if (searchResponse.data.isNotEmpty()) {
            val track = searchResponse.data[0]

            // Test Mapper toSong()
            val song = track.toSong()
            Log.d(TAG, "[API] Map toSong success: ${song.title} (ID: ${song.songId})")

            // Kiểm tra tính đúng đắn của logic audioUrl (phải rỗng)
            if (song.audioUrl.isNotEmpty()) {
                Log.e(TAG, "[API] ERROR: audioUrl should be empty for Deezer tracks!")
            }

            // Test Mapper toArtist()
            val artist = track.artist?.toArtist()
            Log.d(TAG, "[API] Map toArtist success: ${artist?.name}")

            // Test Get Detail
            val detail = apiService.getTrack(track.id)
            Log.d(TAG, "[API] Get Detail success: ${detail.title}")
        } else {
            Log.w(TAG, "[API] No data returned from Deezer. Check Internet connection.")
        }
    }

    private suspend fun testAuthRepository() {
        val repo = AuthRepository()
        val currentUser = repo.getCurrentUser()
        Log.d(TAG, "Current Firebase User: ${currentUser?.email ?: "No user signed in"}")
    }

    private suspend fun testUserRepository() {
        val userRemote = UserRemoteDataSource()
        val repo = UserRepository(userRemote)

        repo.initializeAppSystem()
        val testUser = User(userId = "u1", username = "tester", email = "test@gmail.com")
        repo.upsertUser(testUser)

        val user = repo.getUserOnce("u1")
        Log.d(TAG, "User fetched: ${user?.username}, Role: ${user?.role}")

        val admin = repo.getUserOnce("admin_root")
        Log.d(TAG, "Admin root exists: ${admin != null}")
    }

    private suspend fun testSongRepository() {
        val repo = SongRepository()
        Log.d(TAG, "▶ Testing: Song Repository...")

        // 1. Tìm kiếm bài hát trên Deezer
        val result = repo.searchSongs("Nơi Này Có Anh").first { it.status != Status.LOADING }

        if (result.status == Status.SUCCESS && !result.data.isNullOrEmpty()) {
            val songFromDeezer = result.data[0]
            Log.d(TAG, "Found song to save: ${songFromDeezer.title}")

            // 2. THỰC HIỆN LƯU VÀO FIREBASE
            Log.d(TAG, "Saving song '${songFromDeezer.title}' to Firebase...")
            val isSaved = repo.saveSong(songFromDeezer)

            if (isSaved) {
                Log.d(TAG, "✅ Save to Firebase SUCCESS!")

                // 3. Kiểm tra lại bằng cách lấy thử từ Firebase
                val savedSong = repo.getSongById(songFromDeezer.songId)
                Log.d(TAG, "Fetched back from Firebase: ${savedSong?.title} - isOnline: ${savedSong?.isOnline}")
            } else {
                Log.e(TAG, "❌ Save to Firebase FAILED!")
            }
        }
    }

    private suspend fun testArtistRepository() {
        val repo = ArtistRepository(ArtistRemoteDataSource())
        val artist = Artist(artistId = "artist_1", name = "Mono")
        repo.upsertArtist(artist)

        val searchResults = repo.searchArtists("Mono").first()
        Log.d(TAG, "Artists found with query 'Mono': ${searchResults.size}")
    }

    private suspend fun testPlaylistRepository() {
        val repo = PlaylistRepository(PlaylistRemoteDataSource(), PlaylistSongDataSource())
        val playlist = Playlist(playlistId = "pl_1", name = "My Best Songs", userId = "debug_user_1")
        repo.upsertPlaylist(playlist)

        repo.addSongToPlaylist("pl_1", "song_abc_123", 0)
        val songsInPlaylist = repo.watchPlaylistSongs("pl_1").first()
        Log.d(TAG, "Songs in playlist 'pl_1': ${songsInPlaylist.size}")
    }

    private suspend fun testFavoriteRepository() {
        val repo = FavoriteRepository()
        repo.addToFavorite("debug_user_1", "song_123")
        val isFav = repo.isFavorite("debug_user_1", "song_123")
        Log.d(TAG, "Is song_123 favorite? $isFav")

        repo.removeFromFavorite("debug_user_1", "song_123")
        val isFavAfter = repo.isFavorite("debug_user_1", "song_123")
        Log.d(TAG, "Is favorite after removal? $isFavAfter")
    }

    private suspend fun testRecentlyPlayed() {
        val repo = RecentlyPlayedRepository(RecentlyPlayedDataSource())
        val record = RecentlyPlayed(
            userId = "debug_user_1",
            songId = "song_123",
            playedAt = System.currentTimeMillis()
        )
        repo.addPlayed(record)

        val history = repo.watchUserRecent("debug_user_1").first()
        Log.d(TAG, "Recently played items: ${history.size}")
    }
}