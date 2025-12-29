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
    onSongClick: (String) -> Unit = {},
    onPlaylistClick: (title: String, subtitle: String) -> Unit = { _, _ -> }) {
    // State from ViewModel
    val searchState by viewModel.searchState.collectAsState()
    val currentQuery by viewModel.currentQuery.collectAsState()

    // Local UI state
    var searchQuery by remember { mutableStateOf("") }
    var searchHistory by remember { mutableStateOf(getMockHistory()) }
    var suggestions by remember { mutableStateOf<List<SearchSuggestion>>(emptyList()) }
    var currentFilter by remember { mutableStateOf(SearchFilter.ALL) }

    // Convert searchState to searchResults
    val searchResults = remember(searchState, currentFilter) {
        when (searchState.status) {
            Status.SUCCESS -> {
                val songs = searchState.data?.map { viewModel.toSearchResultItem(it) } ?: emptyList()
                when (currentFilter) {
                    SearchFilter.ALL -> songs // Only show songs for now (Deezer API)
                    SearchFilter.SONG -> songs
                    SearchFilter.ARTIST -> emptyList() // TODO: Add artist search later
                    SearchFilter.PLAYLIST -> emptyList() // TODO: Add playlist search later
                }
            }
            else -> emptyList()
        }
    }

    val isLoading = searchState.status == Status.LOADING

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
                    // Thêm vào history
                    searchHistory = listOf(SearchHistoryItem(query)) +
                            searchHistory.filter { it.query != query }.take(9)

                    // Clear suggestions
                    suggestions = emptyList()

                    // Call ViewModel to search
                    viewModel.searchSongs(query)
                } else {
                    viewModel.clearSearch()
                }
            },
            modifier = Modifier
                .fillMaxWidth()

        )

        // Filter Chips (chỉ hiện khi có results)
        if (searchResults.isNotEmpty() || searchState.status == Status.SUCCESS) {
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

            searchState.status == Status.ERROR -> {
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
                            text = searchState.message ?: "Đã xảy ra lỗi",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            searchQuery.isEmpty() && searchHistory.isEmpty() -> EmptyState()

            searchQuery.isEmpty() && searchHistory.isNotEmpty() -> {
                SearchHistoryList(
                    history = searchHistory,
                    onItemClick = { query ->
                        searchQuery = query
                        viewModel.searchSongs(query)
                    },
                    onRemoveItem = { query ->
                        searchHistory = searchHistory.filter { it.query != query }
                    },
                    onClearAll = {
                        searchHistory = emptyList()
                    }
                )
            }

            searchQuery.isNotEmpty() && suggestions.isNotEmpty() && searchResults.isEmpty() && !isLoading -> {
                SuggestionsList(
                    suggestions = suggestions,
                    onSuggestionClick = { text ->
                        searchQuery = text
                        viewModel.searchSongs(text)
                    },
                    onArrowClick = { text ->
                        searchQuery = text
                    }
                )
            }

            searchResults.isNotEmpty() -> {
                SearchResultsList(
                    results = searchResults,
                    onSongClick = onSongClick,
                    onPlaylistClick = onPlaylistClick
                )
            }

            searchQuery.isNotEmpty() && searchResults.isEmpty() && !isLoading && searchState.status == Status.SUCCESS -> {
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
