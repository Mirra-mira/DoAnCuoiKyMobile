package com.example.doancuoikymobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doancuoikymobile.model.Song
import com.example.doancuoikymobile.player.getDurationFormatted
import com.example.doancuoikymobile.repository.Resource
import com.example.doancuoikymobile.repository.SongRepository
import com.example.doancuoikymobile.ui.search.SearchResultItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Search Screen
 * Handles song search from Deezer API
 */
class SearchViewModel(
    private val songRepository: SongRepository = SongRepository()
) : ViewModel() {

    // Search state
    private val _searchState = MutableStateFlow<Resource<List<Song>>>(Resource.loading(null))
    val searchState: StateFlow<Resource<List<Song>>> = _searchState.asStateFlow()

    // Current search query
    private val _currentQuery = MutableStateFlow<String>("")
    val currentQuery: StateFlow<String> = _currentQuery.asStateFlow()

    /**
     * Search songs from Deezer API
     */
    fun searchSongs(query: String) {
        if (query.isBlank()) {
            _searchState.value = Resource.success(emptyList())
            _currentQuery.value = ""
            return
        }

        _currentQuery.value = query
        _searchState.value = Resource.loading(_searchState.value.data)

        viewModelScope.launch {
            songRepository.searchSongs(query).collect { resource ->
                _searchState.value = resource
            }
        }
    }

    /**
     * Clear search results
     */
    fun clearSearch() {
        _currentQuery.value = ""
        _searchState.value = Resource.success(emptyList())
    }

    /**
     * Convert Song model to SearchResultItem.Song
     * Note: Artist name is not available in Song model, using "Artist" as placeholder
     */
    fun toSearchResultItem(song: Song): SearchResultItem.Song {
        return SearchResultItem.Song(
            id = song.songId,
            title = song.title,
            subtitle = "Artist", // TODO: Get artist name from ArtistRepository if needed
            duration = song.getDurationFormatted()
        )
    }
}

