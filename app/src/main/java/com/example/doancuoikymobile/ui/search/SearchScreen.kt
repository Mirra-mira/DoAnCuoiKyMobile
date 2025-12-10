package com.example.doancuoikymobile.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import androidx.compose.material3.MaterialTheme

data class SearchHistoryItem(
    val query: String
)

data class SearchSuggestion(
    val text: String
)

enum class SearchFilter(val displayName: String) {
    ALL("All"),
    SONG("Song"),
    ARTIST("Artist"),
    PLAYLIST("Playlist")
}


sealed class SearchResultItem {
    abstract val id: String
    abstract val title: String
    abstract val subtitle: String

    data class Song(
        override val id: String,
        override val title: String,
        override val subtitle: String,
        val duration: String = "3:45"
    ) : SearchResultItem()

    data class Artist(
        override val id: String,
        override val title: String,
        override val subtitle: String = "Artist"
    ) : SearchResultItem()

    data class Playlist(
        override val id: String,
        override val title: String,
        override val subtitle: String
    ) : SearchResultItem()
}


@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    onSongClick: (String) -> Unit = {},
    onPlaylistClick: (title: String, subtitle: String) -> Unit = { _, _ -> }) {
    // State management đơn giản
    var searchQuery by remember { mutableStateOf("") }
    var searchHistory by remember { mutableStateOf(getMockHistory()) }
    var suggestions by remember { mutableStateOf<List<SearchSuggestion>>(emptyList()) }
    var searchResults by remember { mutableStateOf<List<SearchResultItem>>(emptyList()) }
    var currentFilter by remember { mutableStateOf(SearchFilter.ALL) }
    var isLoading by remember { mutableStateOf(false) }

    // Effect để load suggestions khi query thay đổi
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotEmpty()) {
            delay(300) // Debounce
            suggestions = getMockSuggestions(searchQuery)
        } else {
            suggestions = emptyList()
            searchResults = emptyList()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize().fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Search Bar
        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            onSearch = { query ->
                if (query.isNotEmpty()) {
                    // Thêm vào history
                    searchHistory = listOf(SearchHistoryItem(query)) +
                            searchHistory.filter { it.query != query }.take(9)

                    // Simulate search
                    isLoading = true
                    suggestions = emptyList()

                    // Mock search results
                    searchResults = getMockSearchResults(query, currentFilter)
                    isLoading = false
                }
            },
            modifier = Modifier
                .fillMaxWidth()

        )

        // Filter Chips (chỉ hiện khi có results)
        if (searchResults.isNotEmpty()) {
            FilterChips(
                currentFilter = currentFilter,
                onFilterChange = { filter ->
                    currentFilter = filter
                    searchResults = getMockSearchResults(searchQuery, filter)
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        when {
            isLoading -> LoadingState()

            searchQuery.isEmpty() && searchHistory.isEmpty() -> EmptyState()

            searchQuery.isEmpty() && searchHistory.isNotEmpty() -> {
                SearchHistoryList(
                    history = searchHistory,
                    onItemClick = { query ->
                        searchQuery = query
                        searchResults = getMockSearchResults(query, currentFilter)
                    },
                    onRemoveItem = { query ->
                        searchHistory = searchHistory.filter { it.query != query }
                    },
                    onClearAll = {
                        searchHistory = emptyList()
                    }
                )
            }

            searchQuery.isNotEmpty() && suggestions.isNotEmpty() && searchResults.isEmpty() -> {
                SuggestionsList(
                    suggestions = suggestions,
                    onSuggestionClick = { text ->
                        searchQuery = text
                        searchResults = getMockSearchResults(text, currentFilter)
                    },
                    onArrowClick = { text ->
                        searchQuery = text
                    }
                )
            }

            searchResults.isNotEmpty() -> {
                SearchResultsList(results = searchResults,
                    onSongClick = onSongClick,
                    onPlaylistClick = onPlaylistClick)
            }
        }
    }
}



private fun getMockHistory() = listOf(
    SearchHistoryItem("Sơn Tùng MTP"),
    SearchHistoryItem("Chillhop"),
    SearchHistoryItem("Acoustic cover"),
    SearchHistoryItem("Running playlist")
)

private fun getMockSuggestions(query: String) = listOf(
    SearchSuggestion("$query"),
    SearchSuggestion("$query - Playlist"),
    SearchSuggestion("$query - Artist"),
)

private fun getMockSearchResults(
    query: String,
    filter: SearchFilter
): List<SearchResultItem> {
    val songs = listOf(
        SearchResultItem.Song("1", "Lạc Trôi", "Sơn Tùng M-TP", "4:25"),
        SearchResultItem.Song("2", "Chúng Ta Của Hiện Tại", "Sơn Tùng M-TP", "5:12"),
        SearchResultItem.Song("3", "Bài Này Chill Phết", "Đen Vâu", "4:49"),
        SearchResultItem.Song("4", "Đen Đá Không Đường", "Đen Vâu", "4:14")
    )

    val artists = listOf(
        SearchResultItem.Artist("a1", "Sơn Tùng M-TP", "Nghệ sĩ"),
        SearchResultItem.Artist("a2", "Đen Vâu", "Nghệ sĩ"),
        SearchResultItem.Artist("a3", "Hoàng Thùy Linh", "Nghệ sĩ")
    )

    val playlists = listOf(
        SearchResultItem.Playlist("p1", "V-Pop Hits", "100 bài hát"),
        SearchResultItem.Playlist("p2", "Chill V-Pop", "50 bài hát"),
        SearchResultItem.Playlist("p3", "Acoustic Vietnam", "75 bài hát")
    )

    return when (filter) {
        SearchFilter.ALL -> songs + artists + playlists
        SearchFilter.SONG -> songs
        SearchFilter.ARTIST -> artists
        SearchFilter.PLAYLIST -> playlists
    }
}