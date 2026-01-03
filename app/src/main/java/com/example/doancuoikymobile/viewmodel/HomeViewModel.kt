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

            val topPlaylists = songRepository.getDeezerGenres()
            val newReleases = songRepository.getNewReleasesFromDeezer()

            combine(
                songRepository.getAllSongs(),
                if (userId != null) recentlyPlayedRepository.watchUserRecent(userId, 10) else flowOf(emptyList())
            ) { firebaseSongs, recentList ->

                val homeSections = mutableListOf<HomeSection>()

                // BẢNG XẾP HẠNG
                if (topPlaylists.isNotEmpty()) {
                    homeSections.add(HomeSection.Genres(topPlaylists))
                }

                // NGHE GẦN ĐÂY
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
                    if (recentSongs.isNotEmpty()) homeSections.add(HomeSection.RecentlyPlayed(recentSongs))
                }

                // PHÁT HÀNH MỚI (Lấy từ Deezer)
                if (newReleases.isNotEmpty()) {
                    val songCards = newReleases.map { song ->
                        ContentCard(
                            id = song.songId,
                            title = song.title,
                            subtitle = song.artistName ?: "Deezer Music",
                            imageUrl = song.coverUrl,
                            type = ContentType.SONG,
                            song = song
                        )
                    }
                    homeSections.add(HomeSection.CustomSection("Newly released", songCards))
                }

                homeSections
            }.collect { sections ->
                _sections.value = sections
            }
        }
    }
}