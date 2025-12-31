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
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import com.google.android.material.R as MaterialR

class LibraryFragment : Fragment() {

    private lateinit var rvLibrary: RecyclerView
    private lateinit var libraryAdapter: LibraryAdapter
    private lateinit var btnPlaylists: TextView
    private lateinit var btnArtists: TextView
    private lateinit var btnSort: View
    private lateinit var tvSortLabel: TextView
    private var isAscending = true

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
        btnSort = view.findViewById(R.id.btnSort)
        tvSortLabel = view.findViewById(R.id.tvSortLabel)

        // 1. Setup Click Handler: Chuyển đến PlaylistDetailFragment khi click vào playlist
        val itemClickHandler: (LibraryModel) -> Unit = { item ->
            val detailFragment = PlaylistDetailFragment.newInstance(item.id, item.title)
            parentFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, detailFragment)
                .addToBackStack("Library")
                .commit()
        }

        // 2. Init Adapter
        libraryAdapter = LibraryAdapter(displayList, itemClickHandler)
        rvLibrary.layoutManager = LinearLayoutManager(context)
        rvLibrary.adapter = libraryAdapter

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            viewModel.loadLibraryData(currentUser.uid)
            observeData()
            updateFilterUI(true)

            btnPlaylists.setOnClickListener {
                updateFilterUI(true)
                val models = viewModel.playlists.value.map { LibraryModel(it.playlistId, it.name, "Playlist") }
                loadData(models)
            }

            btnArtists.setOnClickListener {
                updateFilterUI(false)
                val models = viewModel.artists.value.map { LibraryModel(it.artistId, it.name, "Artist") }
                loadData(models)
            }

            btnSort.setOnClickListener { sortList() }
        }

        return view
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Theo dõi danh sách Playlist
                launch {
                    viewModel.playlists.collect { list ->
                        if (btnPlaylists.tag == "selected") {
                            val models = list.map { LibraryModel(it.playlistId, it.name, "Playlist") }
                            loadData(models)
                        }
                    }
                }
                // Theo dõi danh sách Artist
                launch {
                    viewModel.artists.collect { list ->
                        if (btnArtists.tag == "selected") {
                            val models = list.map { LibraryModel(it.artistId, it.name, "Artist") }
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
    }

    private fun getThemeColor(attr: Int): Int {
        val typedValue = android.util.TypedValue()
        requireContext().theme.resolveAttribute(attr, typedValue, true)
        return typedValue.data
    }

    private fun updateFilterUI(isPlaylistSelected: Boolean) {
        // Lấy màu sắc mặc định từ theme để đảm bảo đồng nhất UI
        val defaultTextColor = getThemeColor(MaterialR.attr.colorOnSurface)
        if (isPlaylistSelected) {
            btnPlaylists.setBackgroundResource(R.drawable.bg_rounded_filled)
            btnPlaylists.setTextColor(Color.BLACK)
            btnPlaylists.tag = "selected"
            btnArtists.setBackgroundResource(R.drawable.bg_rounded_border)
            btnArtists.setTextColor(defaultTextColor)
            btnArtists.tag = "unselected"
        } else {
            btnPlaylists.setBackgroundResource(R.drawable.bg_rounded_border)
            btnPlaylists.setTextColor(defaultTextColor)
            btnPlaylists.tag = "unselected"
            btnArtists.setBackgroundResource(R.drawable.bg_rounded_filled)
            btnArtists.setTextColor(Color.BLACK)
            btnArtists.tag = "selected"
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun sortList() {
        if (isAscending) {
            displayList.sortByDescending { it.title }
            isAscending = false
            tvSortLabel.text = "Z-A"
        } else {
            displayList.sortBy { it.title }
            isAscending = true
            tvSortLabel.text = "A-Z"
        }
        libraryAdapter.notifyDataSetChanged()
    }
}