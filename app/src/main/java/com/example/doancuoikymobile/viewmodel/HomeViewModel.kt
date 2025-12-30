package com.example.doancuoikymobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doancuoikymobile.repository.SongRepository
import com.example.doancuoikymobile.ui.home.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val songRepository: SongRepository = SongRepository()) : ViewModel() {

    private val _sections = MutableStateFlow<List<HomeSection>>(emptyList())
    val sections: StateFlow<List<HomeSection>> = _sections

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            // Lắng nghe dữ liệu bài hát từ Firestore theo thời gian thực
            songRepository.getAllSongs().collect { songs ->
                val homeSections = mutableListOf<HomeSection>()

                // 1. Thể loại nhạc (Dữ liệu cố định theo yêu cầu giao diện)
                homeSections.add(HomeSection.Genres(listOf(
                    Genre("1", "V-Pop"),
                    Genre("2", "Nhạc Trẻ"),
                    Genre("3", "Ballad"),
                    Genre("4", "Rap Việt"),
                    Genre("5", "EDM"),
                    Genre("6", "Acoustic")
                )))

                // 2. Nhạc mới phát hành (Lấy dữ liệu thật từ Firestore)
                if (songs.isNotEmpty()) {
                    val songCards = songs.map { song ->
                        ContentCard(
                            id = song.songId,
                            title = song.title,
                            subtitle = "Bài hát",
                            imageUrl = song.coverUrl,
                            type = ContentType.SONG
                        )
                    }
                    homeSections.add(HomeSection.CustomSection("Phát hành mới", songCards))
                }

                _sections.value = homeSections
            }
        }
    }
}