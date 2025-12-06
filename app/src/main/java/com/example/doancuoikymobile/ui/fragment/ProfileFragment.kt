package com.example.doancuoikymobile.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.doancuoikymobile.R
import com.example.doancuoikymobile.adapter.LibraryAdapter
import com.example.doancuoikymobile.adapter.LibraryModel

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // 1. Xử lý nút Back (Quay lại)
        val btnBack = view.findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            // Quay lại màn hình trước đó
            parentFragmentManager.popBackStack()
        }

        // 2. Setup danh sách Playlist bên dưới
        val rvProfile = view.findViewById<RecyclerView>(R.id.rvProfilePlaylists)

        // Tạo dữ liệu giả cho Profile
        val profileData = arrayListOf(
            LibraryModel("Shazam", "7 likes"),
            LibraryModel("Roadtrip", "4 likes"),
            LibraryModel("Study", "5 likes"),
            LibraryModel("Gym Hits", "12 likes")
        )

        rvProfile.layoutManager = LinearLayoutManager(context)
        rvProfile.adapter = LibraryAdapter(profileData) // Tái sử dụng Adapter cũ

        return view
    }
}