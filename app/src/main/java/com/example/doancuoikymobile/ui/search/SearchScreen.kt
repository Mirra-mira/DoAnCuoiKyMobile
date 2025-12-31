package com.example.doancuoikymobile.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import com.example.doancuoikymobile.model.Song
import com.example.doancuoikymobile.repository.Status
import com.example.doancuoikymobile.viewmodel.SearchViewModel

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
    viewModel: SearchViewModel,
    onSongClick: (Song) -> Unit = {},
    onPlaylistClick: (title: String, subtitle: String) -> Unit = { _, _ -> }) {
    // State from ViewModel
    val searchSongs by viewModel.searchSongs.collectAsState()
    val searchArtists by viewModel.searchArtists.collectAsState()
    val searchPlaylists by viewModel.searchPlaylists.collectAsState()
    val currentQuery by viewModel.currentQuery.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    val searchHistoryState by viewModel.searchHistory.collectAsState()
    var suggestions by remember { mutableStateOf<List<SearchSuggestion>>(emptyList()) }
    var currentFilter by remember { mutableStateOf(SearchFilter.ALL) }

    // Convert search results based on filter
    val searchResults = remember(searchSongs, searchArtists, searchPlaylists, currentFilter) {
        val allResults = mutableListOf<SearchResultItem>()
        when (currentFilter) {
            SearchFilter.ALL -> {
                if (searchSongs.status == Status.SUCCESS) {
                    allResults.addAll(searchSongs.data?.map { viewModel.toSearchResultItem(it) } ?: emptyList())
                }
                if (searchArtists.status == Status.SUCCESS) {
                    allResults.addAll(searchArtists.data?.map { viewModel.toSearchResultItem(it) } ?: emptyList())
                }
                if (searchPlaylists.status == Status.SUCCESS) {
                    allResults.addAll(searchPlaylists.data?.map { viewModel.toSearchResultItem(it) } ?: emptyList())
                }
            }
            SearchFilter.SONG -> {
                if (searchSongs.status == Status.SUCCESS) {
                    allResults.addAll(searchSongs.data?.map { viewModel.toSearchResultItem(it) } ?: emptyList())
                }
            }
            SearchFilter.ARTIST -> {
                if (searchArtists.status == Status.SUCCESS) {
                    allResults.addAll(searchArtists.data?.map { viewModel.toSearchResultItem(it) } ?: emptyList())
                }
            }
            SearchFilter.PLAYLIST -> {
                if (searchPlaylists.status == Status.SUCCESS) {
                    allResults.addAll(searchPlaylists.data?.map { viewModel.toSearchResultItem(it) } ?: emptyList())
                }
            }
        }
        allResults
    }

    val songMap = remember(searchSongs) {
        searchSongs.data?.associateBy { it.songId } ?: emptyMap()
    }

    val isLoading = searchSongs.status == Status.LOADING

    // Effect để load suggestions khi query thay đổi (debounce)
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotEmpty()) {
            delay(300) // Debounce
            suggestions = getMockSuggestions(searchQuery)
        } else {
            suggestions = emptyList()
            viewModel.clearSearch()
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
                    suggestions = emptyList()
                    viewModel.searchAll(query)
                } else {
                    viewModel.clearSearch()
                }
            },
            modifier = Modifier
                .fillMaxWidth()

        )

        // Filter Chips (chỉ hiện khi có results)
        if (searchResults.isNotEmpty() || searchSongs.status == Status.SUCCESS) {
            FilterChips(
                currentFilter = currentFilter,
                onFilterChange = { filter ->
                    currentFilter = filter
                    // Results will update automatically via remember
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        when {
            isLoading -> LoadingState()

            searchSongs.status == Status.ERROR -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Lỗi tìm kiếm",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = searchSongs.message ?: "Đã xảy ra lỗi",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            searchQuery.isEmpty() && searchHistoryState.isEmpty() -> EmptyState()

            searchQuery.isEmpty() && searchHistoryState.isNotEmpty() -> {
                SearchHistoryList(
                    history = searchHistoryState.map { SearchHistoryItem(it) },
                    onItemClick = { query ->
                        searchQuery = query
                        viewModel.searchAll(query)
                    },
                    onRemoveItem = { query ->
                        viewModel.removeFromHistory(query)
                    },
                    onClearAll = {
                        viewModel.clearHistory()
                    }
                )
            }

            searchQuery.isNotEmpty() && suggestions.isNotEmpty() && searchResults.isEmpty() && !isLoading -> {
                SuggestionsList(
                    suggestions = suggestions,
                    onSuggestionClick = { text ->
                        searchQuery = text
                        viewModel.searchAll(text)
                    },
                    onArrowClick = { text ->
                        searchQuery = text
                    }
                )
            }

            searchResults.isNotEmpty() -> {
                SearchResultsList(
                    results = searchResults,
                    songMap = songMap,
                    onSongClick = onSongClick,
                    onPlaylistClick = onPlaylistClick
                )
            }

            searchQuery.isNotEmpty() && searchResults.isEmpty() && !isLoading && searchSongs.status == Status.SUCCESS -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Không tìm thấy kết quả",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}



private fun getMockSuggestions(query: String) = listOf(
    SearchSuggestion("$query"),
    SearchSuggestion("$query - Playlist"),
    SearchSuggestion("$query - Artist"),
)
