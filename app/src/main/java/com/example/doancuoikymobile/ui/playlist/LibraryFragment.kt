package com.example.doancuoikymobile.ui.playlist

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.doancuoikymobile.R
import com.example.doancuoikymobile.adapter.LibraryAdapter
import com.example.doancuoikymobile.adapter.LibraryModel
import com.example.doancuoikymobile.viewmodel.LibraryViewModel
import com.example.doancuoikymobile.model.Playlist
import com.example.doancuoikymobile.utils.EmptyStateHelper
import com.example.doancuoikymobile.ui.dialog.ChoosePlaylistDialog
import com.example.doancuoikymobile.ui.artist.ArtistDetailFragment
import com.example.doancuoikymobile.viewmodel.PlayerViewModel
import com.example.doancuoikymobile.model.Song
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import android.widget.ImageView
import java.util.UUID
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import android.widget.Toast
import com.example.doancuoikymobile.adapter.ItemType
import androidx.fragment.app.activityViewModels

class LibraryFragment : Fragment() {

    private lateinit var rvLibrary: RecyclerView
    private lateinit var libraryAdapter: LibraryAdapter
    private lateinit var btnPlaylists: TextView
    private lateinit var btnArtists: TextView
    private lateinit var btnSongs: TextView
    private lateinit var btnSort: View
    private lateinit var tvSortLabel: TextView
    private lateinit var btnCreatePlaylist: ImageView
    private lateinit var emptyStateView: View
    private var isAscending = true
    private var sortMode = SortMode.RECENTLY_PLAYED  // Thêm mode sắp xếp
    private var currentTab = TabMode.PLAYLISTS

    enum class SortMode {
        RECENTLY_PLAYED,  // Sắp xếp theo thời gian phát
        NAME_AZ,          // Sắp xếp A-Z
        NAME_ZA           // Sắp xếp Z-A
    }

    enum class TabMode {
        PLAYLISTS,
        ARTISTS,
        SONGS
    }

    private var displayList = ArrayList<LibraryModel>()

    private val viewModel: LibraryViewModel by viewModels()
    private val playerViewModel: PlayerViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_library, container, false)

        // Ánh xạ các View từ XML
        rvLibrary = view.findViewById(R.id.rvLibrary)
        btnPlaylists = view.findViewById(R.id.btnPlaylists)
        btnArtists = view.findViewById(R.id.btnArtists)
        btnSongs = view.findViewById(R.id.btnSongs)
        btnSort = view.findViewById(R.id.btnSort)
        tvSortLabel = view.findViewById(R.id.tvSortLabel)
        btnCreatePlaylist = view.findViewById(R.id.btnCreatePlaylist)
        emptyStateView = view.findViewById(R.id.emptyStateLibrary)

        // Setup Click Handler: Chuyển đến PlaylistDetailFragment khi click vào playlist
        val itemClickHandler: (LibraryModel) -> Unit = { item ->
            val fragment: Fragment? = when (item.type) {
                ItemType.ARTIST -> ArtistDetailFragment.newInstance(item.id)
                ItemType.PLAYLIST -> PlaylistDetailFragment.newInstance(item.id, item.title)
                // Trong itemClickHandler, tìm nhánh ItemType.SONG và sửa thành:
                ItemType.SONG -> {
                    // 1. Hiển thị thông báo nhỏ để người dùng biết đang chuẩn bị tải nhạc
                    Toast.makeText(context, "Loading track...", Toast.LENGTH_SHORT).show()

                    // 2. Ép PlayerViewModel fetch lại bài hát mới nhất từ Firebase dựa trên ID
                    // Việc này giúp lấy lại audioUrl hoặc previewUrl mới nhất (tránh lỗi 403 do link hết hạn)
                    val songToPlay = Song(
                        songId = item.id,
                        title = item.title,
                        artistName = item.subtitle,
                        audioUrl = "" // Để trống để PlayerViewModel hiểu là cần phải fetch từ Repository
                    )

                    // 3. Gọi phát nhạc
                    playerViewModel.playSong(songToPlay)

                    null
                }
            }

            fragment?.let { targetFragment ->
                parentFragmentManager.beginTransaction()
                    .replace(R.id.frameLayout, targetFragment)
                    .addToBackStack("Library")
                    .commit()
            }
        }

        // Setup Like/Follow handler
        val currentUser = FirebaseAuth.getInstance().currentUser
        val likeClickHandler: (LibraryModel) -> Unit = { item ->
            if (currentUser != null) {
                when (currentTab) {
                    TabMode.SONGS -> viewModel.toggleLikeSong(currentUser.uid, item.id)
                    TabMode.ARTISTS -> viewModel.toggleFollowArtist(currentUser.uid, item.id)
                    else -> {}
                }
            }
        }

        libraryAdapter = LibraryAdapter(
            displayList,
            onItemClick = itemClickHandler,
            onAddClick = { item ->
                // Logic khi nhấn nút (+)
                showChoosePlaylistDialog(item.id)
            },
            onLikeClick = likeClickHandler
        )
        rvLibrary.layoutManager = LinearLayoutManager(context)
        rvLibrary.adapter = libraryAdapter

        if (currentUser != null) {
            viewModel.loadLibraryData(currentUser.uid)
            observeData()
            updateFilterUI()

            // Nút tạo Playlist mới: mở dialog hoặc activity
            btnCreatePlaylist.setOnClickListener {
                showCreatePlaylistDialog(currentUser.uid)
            }

            btnPlaylists.setOnClickListener {
                currentTab = TabMode.PLAYLISTS
                updateFilterUI()
                sortMode = SortMode.RECENTLY_PLAYED
                tvSortLabel.text = "Recently played"
                // Trigger data load immediately
                viewModel.playlists.value.let { playlists ->
                    val models = playlists.map { LibraryModel(
                        id = it.playlistId,
                        title = it.name,
                        subtitle = "Playlist",
                        type = ItemType.PLAYLIST
                    ) }
                    loadData(models)
                }
            }

            btnArtists.setOnClickListener {
                currentTab = TabMode.ARTISTS
                updateFilterUI()
                sortMode = SortMode.RECENTLY_PLAYED
                tvSortLabel.text = "Recently played"

                viewModel.followedArtists.value.let { artists ->
                    val models = artists.map { LibraryModel(
                        id = it.artistId,
                        title = it.name,
                        subtitle = "Artist",
                        type = ItemType.ARTIST
                    ) }
                    loadData(models)
                }
            }

            btnSongs.setOnClickListener {
                currentTab = TabMode.SONGS
                updateFilterUI()
                sortMode = SortMode.RECENTLY_PLAYED
                tvSortLabel.text = "Recently played"
                // Trigger data load immediately
                viewModel.recentlyPlayed.value.let { songs ->
                    val models = songs.map { recent ->
                        LibraryModel(
                            id = recent.songId,
                            title = recent.songId,
                            subtitle = "Recently Played",
                            type = ItemType.SONG
                        )
                    }
                    loadData(models)
                    // Also prepare liked songs for non-recent mode
                    if (sortMode == SortMode.RECENTLY_PLAYED) {
                        viewModel.likedSongs.value.let { liked ->
                            val likedModels = liked.map { song ->
                                LibraryModel(
                                    id = song.songId,
                                    title = song.title,
                                    subtitle = song.artistName ?: "Unknown Artist",
                                    type = ItemType.SONG
                                )
                            }
                            loadData(likedModels)
                        }
                    }
                }
            }

            btnSort.setOnClickListener { sortList() }
        }

        return view
    }

    private fun observeData() {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

            // Recently Played Songs (cho tab Songs)
            launch {
                viewModel.recentlyPlayed.collect { recentList ->
                    if (currentTab == TabMode.SONGS && sortMode == SortMode.RECENTLY_PLAYED) {
                        val allSongs = viewModel.allSongs.value
                        val models = recentList.map { recent ->
                            val songInfo = allSongs.find { it.songId == recent.songId }
                            LibraryModel(
                                id = recent.songId,
                                title = songInfo?.title ?: "Unknown Song",
                                subtitle = songInfo?.artistName ?: "Unknown Artist",
                                type = ItemType.SONG
                            )
                        }
                        loadData(models)
                    }
                }
            }

            // Songs (hiển thị tất cả bài hát khi ở tab Songs & không phải recently)
            launch {
                viewModel.likedSongs.collect { list ->
                    if (currentTab == TabMode.SONGS && sortMode != SortMode.RECENTLY_PLAYED) {
                        val models = list.map { song ->
                            LibraryModel(
                                id = song.songId,
                                title = song.title,
                                subtitle = song.artistName ?: "Unknown Artist",
                                type = ItemType.SONG
                            )
                        }
                        loadData(models)
                    }
                }
            }

            // Playlist (hiển thị khi đang ở tab Playlist)
            launch {
                viewModel.playlists.collect { list ->
                    if (currentTab == TabMode.PLAYLISTS) {
                        val models = list.map { LibraryModel(it.playlistId, it.name, "Playlist", type = ItemType.PLAYLIST) }
                        loadData(models)
                    }
                }
            }

            // Artist (hiển thị khi đang ở tab Artist)
            launch {
                // Sửa từ viewModel.artists thành viewModel.followedArtists
                viewModel.followedArtists.collect { list ->
                    if (currentTab == TabMode.ARTISTS) {
                        val models = list.map {
                            LibraryModel(
                                id = it.artistId,
                                title = it.name,
                                subtitle = "Artist",
                                type = ItemType.ARTIST,
                                // imageUrl = it.image
                            )
                        }
                        loadData(models)
                    }
                }
            }

            launch {
                viewModel.likedSongs.collect { list ->
                    if (currentTab == TabMode.SONGS && sortMode != SortMode.RECENTLY_PLAYED) {
                        val models = list.map { song ->
                            LibraryModel(
                                song.songId,
                                song.title,
                                song.artistName ?: "Unknown Artist",
                                type = ItemType.SONG,
                                isLiked = true
                            )
                        }
                        loadData(models)
                    }
                }
            }
        }
    }
}


    @SuppressLint("NotifyDataSetChanged")
    private fun loadData(newData: List<LibraryModel>) {
        displayList.clear()
        displayList.addAll(newData)
        libraryAdapter.notifyDataSetChanged()
        
        // Handle empty state
        EmptyStateHelper.handleEmptyState(emptyStateView, rvLibrary, newData.isEmpty())
        // Trong hàm loadData của LibraryFragment
        if (newData.isEmpty()) {
            val message = when(currentTab) {
                TabMode.PLAYLISTS -> "Create your first playlist to get started"
                TabMode.ARTISTS -> "Follow your favorite artists to see them here"
                TabMode.SONGS -> "Like some songs to build your library"
            }
            EmptyStateHelper.updateEmptyState(
                emptyStateView,
                iconResId = R.drawable.ic_music_note,
                title = "Empty Library",
                message = message
            )
        }
    }

    private fun getThemeColor(attr: Int): Int {
        val typedValue = android.util.TypedValue()
        requireContext().theme.resolveAttribute(attr, typedValue, true)
        return typedValue.data
    }

    private fun updateFilterUI() {
        // Lấy màu mặc định từ colors.xml
        val defaultTextColor = ContextCompat.getColor(requireContext(), R.color.off_black)

        // Reset tất cả các button
        listOf(btnPlaylists, btnArtists, btnSongs).forEach { btn ->
            btn.setBackgroundResource(R.drawable.bg_rounded_border)
            btn.setTextColor(defaultTextColor)
            btn.tag = "unselected"
        }

        // Highlight button đang chọn
        when (currentTab) {
            TabMode.PLAYLISTS -> {
                btnPlaylists.setBackgroundResource(R.drawable.bg_rounded_filled)
                btnPlaylists.setTextColor(Color.BLACK)
                btnPlaylists.tag = "selected"
            }
            TabMode.ARTISTS -> {
                btnArtists.setBackgroundResource(R.drawable.bg_rounded_filled)
                btnArtists.setTextColor(Color.BLACK)
                btnArtists.tag = "selected"
            }
            TabMode.SONGS -> {
                btnSongs.setBackgroundResource(R.drawable.bg_rounded_filled)
                btnSongs.setTextColor(Color.BLACK)
                btnSongs.tag = "selected"
            }
        }
    }

    private fun showChoosePlaylistDialog(songId: String) {
        val playlists = viewModel.getCurrentPlaylists()

        if (playlists.isEmpty()) {
            Toast.makeText(requireContext(), "Bạn chưa có playlist nào. Hãy tạo mới!", Toast.LENGTH_SHORT).show()
            return
        }

        val dialog = ChoosePlaylistDialog(
            context = requireContext(),
            playlists = playlists,
            onSelect = { selectedPlaylist ->
                viewModel.addSongToPlaylist(selectedPlaylist.playlistId, songId)
                Toast.makeText(
                    requireContext(),
                    "Đã thêm vào ${selectedPlaylist.name}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
        dialog.show()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun sortList() {
        when (sortMode) {
            SortMode.RECENTLY_PLAYED -> {
                // Chuyển sang sắp xếp A-Z
                displayList.sortBy { it.title }
                sortMode = SortMode.NAME_AZ
                tvSortLabel.text = "A-Z"
            }
            SortMode.NAME_AZ -> {
                // Chuyển sang sắp xếp Z-A
                displayList.sortByDescending { it.title }
                sortMode = SortMode.NAME_ZA
                tvSortLabel.text = "Z-A"
            }
            SortMode.NAME_ZA -> {
                // Quay lại Recently played (mặc định là lấy từ ViewModel theo thứ tự mới nhất)
                // Reload dữ liệu gốc từ ViewModel
                val models = when (currentTab) {
                    TabMode.PLAYLISTS -> viewModel.playlists.value.map { LibraryModel(
                        id = it.playlistId,
                        title = it.name,
                        subtitle = "Playlist",
                        type = ItemType.PLAYLIST
                    )
                    }
                    TabMode.ARTISTS -> viewModel.artists.value.map { LibraryModel(
                        id = it.artistId,
                        title = it.name,
                        subtitle = "Artist",
                        type = ItemType.ARTIST
                    )
                    }
                    TabMode.SONGS -> viewModel.recentlyPlayed.value.map {
                        LibraryModel(
                            id = it.songId,
                            title = it.songId,
                            subtitle = "Recently Played",
                            type = ItemType.SONG
                        )
                    }
                }
                loadData(models)
                sortMode = SortMode.RECENTLY_PLAYED
                tvSortLabel.text = "Recently played"
            }
        }
        libraryAdapter.notifyDataSetChanged()
    }

    private fun showCreatePlaylistDialog(userId: String) {
        // Dialog đơn giản để nhập tên Playlist
        val input = androidx.appcompat.widget.AppCompatEditText(requireContext())
        input.hint = "Playlist name"
        
        AlertDialog.Builder(requireContext())
            .setTitle("Create New Playlist")
            .setView(input)
            .setPositiveButton("Create") { _, _ ->
                val playlistName = input.text.toString().trim()
                if (playlistName.isNotEmpty()) {
                    // Tạo playlist mới - createdAt tự động set bằng System.currentTimeMillis()
                    val newPlaylist = Playlist(
                        playlistId = UUID.randomUUID().toString(),
                        userId = userId,
                        name = playlistName,
                        createdAt = System.currentTimeMillis()
                    )
                    viewModel.createPlaylist(newPlaylist)
                    // ViewModel sẽ phát sự kiện qua Flow, UI tự động cập nhật
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}