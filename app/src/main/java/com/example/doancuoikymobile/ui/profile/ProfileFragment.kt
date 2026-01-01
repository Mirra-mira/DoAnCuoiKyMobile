package com.example.doancuoikymobile.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
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

        // 2. Setup RecyclerView
        val rvProfile = view.findViewById<RecyclerView>(R.id.rvProfilePlaylists)

        // Tạo dữ liệu giả (Dùng lại Model bạn đã tạo)
        val profileData = arrayListOf(
            LibraryModel("Shazam", "7 likes"),
            LibraryModel("Roadtrip", "4 likes"),
            LibraryModel("Study", "5 likes"),
            LibraryModel("Coding Mode", "Playlist • 102 songs")
        )

        rvProfile.layoutManager = LinearLayoutManager(context)

        // FIX: Pass the required 'onItemClick' parameter.
        // For now, it can be an empty lambda if you don't need to handle clicks here.
        rvProfile.adapter = LibraryAdapter(profileData) {


            }

        val btnEditProfile = view.findViewById<TextView>(R.id.btnEditProfile)
        btnEditProfile.setOnClickListener {
            // Navigate to EditProfileFragment
            parentFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, EditProfileFragment())
                .addToBackStack(null) // Cho phép back về ProfileFragment
                .commit()
        }

        return view
    }
}
