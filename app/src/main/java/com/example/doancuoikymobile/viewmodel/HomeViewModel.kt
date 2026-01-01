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

            combine(
                songRepository.getAllSongs(),
                if (userId != null) recentlyPlayedRepository.watchUserRecent(userId, 10) else flowOf(emptyList())
            ) { songs, recentList ->
                val homeSections = mutableListOf<HomeSection>()

                homeSections.add(HomeSection.Genres(listOf(
                    Genre("1", "V-Pop"),
                    Genre("2", "Nhạc Trẻ"),
                    Genre("3", "Ballad"),
                    Genre("4", "Rap Việt"),
                    Genre("5", "EDM"),
                    Genre("6", "Acoustic")
                )))

                if (recentList.isNotEmpty() && userId != null) {
                    val recentSongs = recentList.mapNotNull { recent ->
                        songs.find { it.songId == recent.songId }?.let { song ->
                            RecentlyPlayedItem(
                                id = song.songId,
                                title = song.title,
                                subtitle = "",
                                imageUrl = song.coverUrl,
                                type = ContentType.SONG,
                                playedAt = recent.playedAt,
                                song = song
                            )
                        }
                    }
                    if (recentSongs.isNotEmpty()) {
                        homeSections.add(HomeSection.RecentlyPlayed(recentSongs))
                    }
                }

                if (songs.isNotEmpty()) {
                    val songCards = songs.map { song ->
                        ContentCard(
                            id = song.songId,
                            title = song.title,
                            subtitle = "Bài hát",
                            imageUrl = song.coverUrl,
                            type = ContentType.SONG,
                            song = song
                        )
                    }
                    homeSections.add(HomeSection.CustomSection("Phát hành mới", songCards))
                }

                _sections.value = homeSections
            }.collect { }
        }
    }
}
