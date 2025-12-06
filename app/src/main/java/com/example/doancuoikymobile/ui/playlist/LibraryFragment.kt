package com.example.doancuoikymobile.ui.playlist

import android.R.style.Theme
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.doancuoikymobile.R
import com.example.doancuoikymobile.adapter.LibraryAdapter
import com.example.doancuoikymobile.adapter.LibraryModel
import com.example.doancuoikymobile.ui.profile.ProfileFragment

class LibraryFragment : Fragment() {

    // 1. Khai báo các biến View và Adapter
    private lateinit var rvLibrary: RecyclerView
    private lateinit var libraryAdapter: LibraryAdapter
    private lateinit var btnPlaylists: TextView
    private lateinit var btnArtists: TextView
    private lateinit var btnSort: View
    private lateinit var tvSortLabel: TextView
    private var isAscending = true
    // 2. Khai báo dữ liệu
    // displayList là danh sách ĐANG hiển thị trên màn hình
    private var displayList = ArrayList<LibraryModel>()

    // Dữ liệu giả cho Playlist
    private val playlistData = arrayListOf(
        LibraryModel("Liked Songs", "Playlist • 58 songs"),
        LibraryModel("Front Left", "Playlist • Spotify"),
        LibraryModel("Chill Hits", "Playlist • Spotify"),
        LibraryModel("Sleep", "Playlist • Spotify"),
        LibraryModel("Discover Weekly", "Playlist • Spotify")
    )

    // Dữ liệu giả lập cho Artist
    private val artistData = arrayListOf(
        LibraryModel("Lana Del Rey", "Artist"),
        LibraryModel("Marvin Gaye", "Artist"),
        LibraryModel("Imagine Dragons", "Artist"),
        LibraryModel("Son Tung M-TP", "Artist"),
        LibraryModel("Adele", "Artist")
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_library, container, false)

        // Ánh xạ (Tìm) các View từ XML
        rvLibrary = view.findViewById(R.id.rvLibrary)
        btnPlaylists = view.findViewById(R.id.btnPlaylists)
        btnArtists = view.findViewById(R.id.btnArtists)
        btnSort = view.findViewById(R.id.btnSort)
        tvSortLabel = view.findViewById(R.id.tvSortLabel)
        // Cài đặt Adapter
        // Mặc định ban đầu add dữ liệu Playlist vào displayList
        displayList.addAll(playlistData)
        libraryAdapter = LibraryAdapter(displayList)
        rvLibrary.layoutManager = LinearLayoutManager(context)
        rvLibrary.adapter = libraryAdapter

        // Set giao diện nút bấm mặc định (Playlist)
        updateFilterUI(isPlaylistSelected = true)
        tvSortLabel.text = "A-Z"
        // Xử lý sự kiện khi bấm nút
        btnPlaylists.setOnClickListener {
            if (btnPlaylists.tag != "selected") {
                updateFilterUI(isPlaylistSelected = true)
                loadData(playlistData)
                // Reset lại sort khi chuyển tab nếu muốn
                isAscending = true
            }
        }

        btnArtists.setOnClickListener {
            if (btnArtists.tag != "selected") {
                updateFilterUI(isPlaylistSelected = false)
                loadData(artistData)
                isAscending = true
            }
        }
        btnSort.setOnClickListener {
            sortList()
        }
        val cardAvatar = view.findViewById<View>(R.id.cardAvatar) // Tìm CardView Avatar
        cardAvatar.setOnClickListener {
            // Chuyển sang ProfileFragment
            parentFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, ProfileFragment())
                .addToBackStack(null) // Để user ấn nút Back của điện thoại thì quay lại được
                .commit()
        }
        return view
    }

    // Hàm cập nhật dữ liệu cho RecyclerView
    private fun loadData(newData: List<LibraryModel>) {
        displayList.clear() // Xóa dữ liệu cũ
        displayList.addAll(newData) // Thêm dữ liệu mới
        libraryAdapter.notifyDataSetChanged() // Báo cho Adapter vẽ lại
    }

    // Hàm đổi màu nút bấm (Xanh/Đen)
    private fun updateFilterUI(isPlaylistSelected: Boolean) {
        if (isPlaylistSelected) {
            // --- Chọn Playlist ---
            // Nút Playlist: Nền xanh, Chữ đen, Tag = selected
            btnPlaylists.setBackgroundResource(R.drawable.bg_rounded_filled)
            btnPlaylists.setTextColor(Color.WHITE)
            btnPlaylists.tag = "selected"

            // Nút Artist: Viền trắng, Chữ trắng, Tag = unselected
            btnArtists.setBackgroundResource(R.drawable.bg_rounded_border)
            btnArtists.setTextColor(Color.BLACK)
            btnArtists.tag = "unselected"
        } else {
            // --- Chọn Artist ---
            // Nút Playlist: Viền trắng, Chữ trắng
            btnPlaylists.setBackgroundResource(R.drawable.bg_rounded_border)
            btnPlaylists.setTextColor(Color.BLACK)
            btnPlaylists.tag = "unselected"

            // Nút Artist: Nền xanh, Chữ đen
            btnArtists.setBackgroundResource(R.drawable.bg_rounded_filled)
            btnArtists.setTextColor(Color.WHITE)
            btnArtists.tag = "selected"
        }
    }
    private fun sortList() {
        if (isAscending) {
            // Đang là A-Z, bấm vào sẽ thành Z-A
            displayList.sortByDescending { it.title } // Hàm sắp xếp giảm dần theo Title
            isAscending = false
            tvSortLabel.text = "Z-A"
        } else {
            // Đang là Z-A, bấm vào sẽ thành A-Z
            displayList.sortBy { it.title } // Hàm sắp xếp tăng dần theo Title
            isAscending = true
            tvSortLabel.text = "A-Z"
        }

        // Báo adapter vẽ lại
        libraryAdapter.notifyDataSetChanged()
    }
}