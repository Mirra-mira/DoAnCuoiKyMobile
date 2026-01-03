package com.example.doancuoikymobile.viewmodel

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doancuoikymobile.model.Song
import com.example.doancuoikymobile.model.Artist
import com.example.doancuoikymobile.model.Playlist
import com.example.doancuoikymobile.player.getDurationFormatted
import com.example.doancuoikymobile.repository.Resource
import com.example.doancuoikymobile.repository.Status
import com.example.doancuoikymobile.repository.SongRepository
import com.example.doancuoikymobile.repository.PlaylistRepository
import com.example.doancuoikymobile.repository.AuthRepository
import com.example.doancuoikymobile.data.remote.firebase.PlaylistRemoteDataSource
import com.example.doancuoikymobile.data.remote.firebase.PlaylistSongDataSource
import com.example.doancuoikymobile.data.remote.api.DeezerArtistDataSource
import com.example.doancuoikymobile.data.remote.api.DeezerPlaylistDataSource
import com.example.doancuoikymobile.ui.search.SearchResultItem
import com.example.doancuoikymobile.ui.search.SearchFilter
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel(
    private val songRepository: SongRepository = SongRepository(),
    private val deezerArtistDataSource: DeezerArtistDataSource = DeezerArtistDataSource(),
    private val deezerPlaylistDataSource: DeezerPlaylistDataSource = DeezerPlaylistDataSource(),
    private val playlistRepository: PlaylistRepository = PlaylistRepository(
        PlaylistRemoteDataSource(FirebaseFirestore.getInstance()),
        PlaylistSongDataSource(FirebaseFirestore.getInstance())
    ),
    private val authRepository: AuthRepository = AuthRepository(),
    val context: Context? = null
) : ViewModel() {

    private val prefs: SharedPreferences? = context?.getSharedPreferences("search_history", Context.MODE_PRIVATE)

    // Raw search results from repositories
    private val _searchSongs = MutableStateFlow<Resource<List<Song>>>(Resource.loading(null))
    val searchSongs: StateFlow<Resource<List<Song>>> = _searchSongs.asStateFlow()

    private val _searchArtists = MutableStateFlow<Resource<List<Artist>>>(Resource.loading(null))
    val searchArtists: StateFlow<Resource<List<Artist>>> = _searchArtists.asStateFlow()

    private val _searchPlaylists = MutableStateFlow<Resource<List<Playlist>>>(Resource.loading(null))
    val searchPlaylists: StateFlow<Resource<List<Playlist>>> = _searchPlaylists.asStateFlow()

    private val _currentQuery = MutableStateFlow<String>("")
    val currentQuery: StateFlow<String> = _currentQuery.asStateFlow()

    private val _currentFilter = MutableStateFlow<SearchFilter>(SearchFilter.ALL)
    val currentFilter: StateFlow<SearchFilter> = _currentFilter.asStateFlow()

    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()

    init {
        loadSearchHistory()
    }

    fun loadSearchHistory() {
        val history = prefs?.getStringSet("history", emptySet())?.toList()?.take(10) ?: emptyList()
        _searchHistory.value = history
    }

    fun removeFromHistory(query: String) {
        val updated = _searchHistory.value.filter { it != query }
        _searchHistory.value = updated
        prefs?.edit()?.putStringSet("history", updated.toSet())?.apply()
    }

    fun clearHistory() {
        _searchHistory.value = emptyList()
        prefs?.edit()?.putStringSet("history", emptySet())?.apply()
    }

    private fun saveSearchHistory(query: String) {
        val current = _searchHistory.value.toMutableList()
        current.remove(query)
        current.add(0, query)
        val limited = current.take(10)
        _searchHistory.value = limited
        prefs?.edit()?.putStringSet("history", limited.toSet())?.apply()
    }

    fun setFilter(filter: SearchFilter) {
        _currentFilter.value = filter
    }

    fun searchAll(query: String) {
        if (query.isBlank()) {
            _searchSongs.value = Resource.success(emptyList())
            _searchArtists.value = Resource.success(emptyList())
            _searchPlaylists.value = Resource.success(emptyList())
            _currentQuery.value = ""
            _currentFilter.value = SearchFilter.ALL
            return
        }

        _currentQuery.value = query
        _currentFilter.value = SearchFilter.ALL
        saveSearchHistory(query)
        
        searchSongsOnly(query)
        searchArtistsOnly(query)
        searchPlaylistsOnly(query)
    }

    fun searchSongsOnly(query: String) {
        _searchSongs.value = Resource.loading(null)
        viewModelScope.launch {
            songRepository.searchSongs(query).collect { resource ->
                _searchSongs.value = resource
            }
        }
    }

    private fun searchArtistsOnly(query: String) {
        _searchArtists.value = Resource.loading(null)
        viewModelScope.launch {
            try {
                deezerArtistDataSource.searchArtists(query).collect { artists ->
                    _searchArtists.value = Resource.success(artists)
                }
            } catch (e: Exception) {
                _searchArtists.value = Resource.error(e.message ?: "Error", null)
            }
        }
    }

    private fun searchPlaylistsOnly(query: String) {
        _searchPlaylists.value = Resource.loading(null)
        viewModelScope.launch {
            try {
                deezerPlaylistDataSource.searchPlaylists(query).collect { playlists ->
                    _searchPlaylists.value = Resource.success(playlists)
                }
            } catch (e: Exception) {
                _searchPlaylists.value = Resource.error(e.message ?: "Error", null)
            }
        }
    }

    fun clearSearch() {
        _currentQuery.value = ""
        _currentFilter.value = SearchFilter.ALL
        _searchSongs.value = Resource.success(emptyList())
        _searchArtists.value = Resource.success(emptyList())
        _searchPlaylists.value = Resource.success(emptyList())
    }

    /**
     * Get filtered songs based on current filter
     */
    fun getFilteredSongs(): List<Song> {
        return when (_currentFilter.value) {
            SearchFilter.SONG -> _searchSongs.value.data ?: emptyList()
            SearchFilter.ALL -> _searchSongs.value.data ?: emptyList()
            else -> emptyList()
        }
    }

    /**
     * Get filtered artists based on current filter
     */
    fun getFilteredArtists(): List<Artist> {
        return when (_currentFilter.value) {
            SearchFilter.ARTIST -> _searchArtists.value.data ?: emptyList()
            SearchFilter.ALL -> _searchArtists.value.data ?: emptyList()
            else -> emptyList()
        }
    }

    /**
     * Get filtered playlists based on current filter
     */
    fun getFilteredPlaylists(): List<Playlist> {
        return when (_currentFilter.value) {
            SearchFilter.PLAYLIST -> _searchPlaylists.value.data ?: emptyList()
            SearchFilter.ALL -> _searchPlaylists.value.data ?: emptyList()
            else -> emptyList()
        }
    }

    /**
     * Get overall loading state considering all search types
     */
    fun isAnyLoading(): Boolean {
        return _searchSongs.value.status == Status.LOADING ||
               _searchArtists.value.status == Status.LOADING ||
               _searchPlaylists.value.status == Status.LOADING
    }

    fun toSearchResultItem(song: Song): SearchResultItem.Song {
        return SearchResultItem.Song(
            id = song.songId,
            title = song.title,
            subtitle = song.artistName ?: "Unknown Artist",
            duration = song.getDurationFormatted()
        )
    }

    fun toSearchResultItem(artist: Artist): SearchResultItem.Artist {
        return SearchResultItem.Artist(
            id = artist.artistId,
            title = artist.name,
            subtitle = "Artist"
        )
    }

    fun toSearchResultItem(playlist: Playlist): SearchResultItem.Playlist {
        return SearchResultItem.Playlist(
            id = playlist.playlistId,
            title = playlist.name,
            subtitle = "Playlist"
        )
    }
}

