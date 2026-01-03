package com.example.doancuoikymobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doancuoikymobile.repository.SongRepository
import com.example.doancuoikymobile.repository.RecentlyPlayedRepository
import com.example.doancuoikymobile.repository.AuthRepository
import com.example.doancuoikymobile.data.remote.firebase.RecentlyPlayedDataSource
import com.example.doancuoikymobile.ui.home.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

class HomeViewModel(
    private val songRepository: SongRepository = SongRepository(),
    private val recentlyPlayedRepository: RecentlyPlayedRepository = RecentlyPlayedRepository(RecentlyPlayedDataSource()),
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _sections = MutableStateFlow<List<HomeSection>>(emptyList())
    val sections: StateFlow<List<HomeSection>> = _sections

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {

            val currentUser = authRepository.getCurrentUser()
            val userId = currentUser?.uid

            // 🔹 Lấy dữ liệu Deezer song song (không phải Flow)
            val genres = songRepository.getDeezerGenres()
            val newReleases = songRepository.getNewReleasesFromDeezer()

            combine(
                songRepository.getAllSongs(), // Firebase songs
                if (userId != null)
                    recentlyPlayedRepository.watchUserRecent(userId, 10)
                else
                    flowOf(emptyList())
            ) { firebaseSongs, recentList ->

                val homeSections = mutableListOf<HomeSection>()

                /* =========================
                 * 1. THỂ LOẠI (DEEZER)
                 * ========================= */
                if (genres.isNotEmpty()) {
                    homeSections.add(
                        HomeSection.Genres(genres)
                    )
                }

                /* =========================
                 * 2. NGHE GẦN ĐÂY
                 * ========================= */
                if (recentList.isNotEmpty() && userId != null) {
                    val recentSongs = recentList.mapNotNull { recent ->
                        firebaseSongs.find { it.songId == recent.songId }?.let { song ->
                            RecentlyPlayedItem(
                                id = song.songId,
                                title = song.title,
                                subtitle = song.artistName ?: "",
                                imageUrl = song.coverUrl,
                                type = ContentType.SONG,
                                playedAt = recent.playedAt,
                                song = song
                            )
                        }
                    }

                    if (recentSongs.isNotEmpty()) {
                        homeSections.add(
                            HomeSection.RecentlyPlayed(recentSongs)
                        )
                    }
                }

                /* =========================
                 * 3. MỚI PHÁT HÀNH (DEEZER)
                 * ========================= */
                if (newReleases.isNotEmpty()) {
                    val songCards = newReleases.map { song ->
                        ContentCard(
                            id = song.songId,
                            title = song.title,
                            subtitle = song.artistName ?: "Artist",
                            imageUrl = song.coverUrl,
                            type = ContentType.SONG,
                            song = song
                        )
                    }

                    homeSections.add(
                        HomeSection.CustomSection(
                            title = "Mới phát hành",
                            items = songCards
                        )
                    )
                }

                homeSections
            }.collect { sections ->
                _sections.value = sections
            }
        }
    }
}
