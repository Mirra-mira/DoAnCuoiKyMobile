package com.example.doancuoikymobile.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.doancuoikymobile.R
import com.example.doancuoikymobile.adapter.LibraryAdapter
import com.example.doancuoikymobile.adapter.LibraryModel
import com.example.doancuoikymobile.ui.playlist.PlaylistDetailFragment
import com.example.doancuoikymobile.viewmodel.LibraryViewModel

class ProfileFragment : Fragment() {

    private val viewModel: LibraryViewModel by viewModels()

    private lateinit var libraryAdapter: LibraryAdapter
    private var displayList = ArrayList<LibraryModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        val rvProfile = view.findViewById<RecyclerView>(R.id.rvProfilePlaylists)

        // Khởi tạo Adapter với logic click mở màn hình chi tiết
        libraryAdapter = LibraryAdapter(displayList) { item ->
            // Truyền ID và Title vào màn hình chi tiết
            val detailFragment = PlaylistDetailFragment.newInstance(item.id, item.title)
            parentFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, detailFragment)
                .addToBackStack(null)
                .commit()
        }

        rvProfile?.layoutManager = LinearLayoutManager(context)
        rvProfile?.adapter = libraryAdapter

        // 2. Nạp dữ liệu giả (Mock Data)
        // Đảm bảo truyền đủ 3 tham số (id, title, subtitle) để không bị lỗi "No value passed"
        setupMockData()

        return view
    }

    private fun setupMockData() {
        // Dữ liệu giả lập theo cấu trúc của LibraryViewModel
        val mockProfilePlaylists = arrayListOf(
            LibraryModel("p1", "My Favorite Songs", "Playlist • 15 songs"),
            LibraryModel("p2", "Sky Tour Mix", "Playlist • 22 songs"),
            LibraryModel("p3", "Late Night Chill", "Playlist • 10 songs"),
            LibraryModel("p4", "Top V-Pop 2025", "Playlist • 58 songs")
        )

        displayList.clear()
        displayList.addAll(mockProfilePlaylists)
        libraryAdapter.notifyDataSetChanged()
    }
}