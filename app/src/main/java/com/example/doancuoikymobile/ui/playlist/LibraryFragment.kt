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
import com.example.doancuoikymobile.model.Song
import com.example.doancuoikymobile.utils.EmptyStateHelper
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import android.widget.ImageView
import java.util.UUID
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat

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

    // Sử dụng ViewModel để quản lý dữ liệu từ Firestore
    private val viewModel: LibraryViewModel by viewModels()

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
        btnCreatePlaylist = view.findViewById(R.id.btnCreatePlaylist)  // Nút (+) tạo playlist mới
        emptyStateView = view.findViewById(R.id.emptyStateLibrary)

        // 1. Setup Click Handler: Chuyển đến PlaylistDetailFragment khi click vào playlist
        val itemClickHandler: (LibraryModel) -> Unit = { item ->
            val detailFragment = PlaylistDetailFragment.newInstance(item.id, item.title)
            parentFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, detailFragment)
                .addToBackStack("Library")
                .commit()
        }

        // 2. Init Adapter
        libraryAdapter = LibraryAdapter(
            displayList,
            onItemClick = itemClickHandler,
            onAddClick = { /* TODO: handle add button click */ }
        )
        rvLibrary.layoutManager = LinearLayoutManager(context)
        rvLibrary.adapter = libraryAdapter

        val currentUser = FirebaseAuth.getInstance().currentUser
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
            }

            btnArtists.setOnClickListener {
                currentTab = TabMode.ARTISTS
                updateFilterUI()
                sortMode = SortMode.RECENTLY_PLAYED
                tvSortLabel.text = "Recently played"
            }

            btnSongs.setOnClickListener {
                currentTab = TabMode.SONGS
                updateFilterUI()
                sortMode = SortMode.RECENTLY_PLAYED
                tvSortLabel.text = "Recently played"
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
                viewModel.recentlyPlayed.collect { list ->
                    if (currentTab == TabMode.SONGS && sortMode == SortMode.RECENTLY_PLAYED) {
                        val models = list.map { recent ->
                            // Tìm thông tin bài hát từ ViewModel
                            LibraryModel(
                                id = recent.songId,
                                title = recent.songId,  // Tên bài hát sẽ được update từ songs
                                subtitle = "Recently Played"
                            )
                        }
                        loadData(models)
                    }
                }
            }

            // Songs (hiển thị tất cả bài hát khi ở tab Songs & không phải recently)
            launch {
                viewModel.allSongs.collect { list ->
                    if (currentTab == TabMode.SONGS && sortMode != SortMode.RECENTLY_PLAYED) {
                        val models = list.map { song ->
                            LibraryModel(
                                id = song.songId,
                                title = song.title,
                                subtitle = song.artistName ?: "Unknown Artist"
                            )
                        }
                        loadData(models)
                    }
                }
            }

            // Playlist (chỉ khi đang ở tab Playlist & không phải recently)
            launch {
                viewModel.playlists.collect { list ->
                    if (currentTab == TabMode.PLAYLISTS
                        && sortMode != SortMode.RECENTLY_PLAYED
                    ) {
                        val models = list.map {
                            LibraryModel(it.playlistId, it.name, "Playlist")
                        }
                        loadData(models)
                    }
                }
            }

            // Artist (chỉ khi đang ở tab Artist & không phải recently)
            launch {
                viewModel.artists.collect { list ->
                    if (currentTab == TabMode.ARTISTS
                        && sortMode != SortMode.RECENTLY_PLAYED
                    ) {
                        val models = list.map {
                            LibraryModel(it.artistId, it.name, "Artist")
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
        if (newData.isEmpty()) {
            EmptyStateHelper.updateEmptyState(
                emptyStateView,
                iconResId = R.drawable.ic_music_note,
                title = "No Items",
                message = "Create your first playlist to get started"
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
                    TabMode.PLAYLISTS -> viewModel.playlists.value.map { LibraryModel(it.playlistId, it.name, "Playlist") }
                    TabMode.ARTISTS -> viewModel.artists.value.map { LibraryModel(it.artistId, it.name, "Artist") }
                    TabMode.SONGS -> viewModel.recentlyPlayed.value.map { 
                        LibraryModel(it.songId, it.songId, "Recently Played") 
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