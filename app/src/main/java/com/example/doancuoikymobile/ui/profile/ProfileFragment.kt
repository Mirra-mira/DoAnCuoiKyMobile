package com.example.doancuoikymobile.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.doancuoikymobile.R
import com.example.doancuoikymobile.adapter.LibraryAdapter
import com.example.doancuoikymobile.adapter.LibraryModel
import com.example.doancuoikymobile.repository.AuthRepository
import com.example.doancuoikymobile.ui.playlist.PlaylistDetailFragment
import com.example.doancuoikymobile.viewmodel.LibraryViewModel
import com.example.doancuoikymobile.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch
import android.widget.Toast

class ProfileFragment : Fragment() {

    private val profileViewModel: ProfileViewModel by viewModels()
    private val libraryViewModel: LibraryViewModel by viewModels()
    private val authRepository = AuthRepository()

    private lateinit var libraryAdapter: LibraryAdapter
    private var displayList = ArrayList<LibraryModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        val rvProfile = view.findViewById<RecyclerView>(R.id.rvProfilePlaylists)
        val tvDisplayName = view.findViewById<TextView>(R.id.tvDisplayName)
        val ivProfileAvatar = view.findViewById<ImageView>(R.id.ivProfileAvatar)

        libraryAdapter = LibraryAdapter(
            displayList,
            onItemClick = { item ->
                val detailFragment = PlaylistDetailFragment.newInstance(item.id, item.title)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.frameLayout, detailFragment)
                    .addToBackStack(null)
                    .commit()
            },
            onAddClick = { item ->
                // Nếu chưa cần chức năng thêm thì để trống hoặc show toast
                Toast.makeText(requireContext(), "Add ${item.title}", Toast.LENGTH_SHORT).show()
            }
        )

        rvProfile.layoutManager = LinearLayoutManager(context)
        rvProfile.adapter = libraryAdapter

        val firebaseUser = authRepository.getCurrentUser()
        if (firebaseUser != null) {
            profileViewModel.loadUser()
            libraryViewModel.loadLibraryData(firebaseUser.uid)
        }

        // Collect user data to display avatar and name
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                profileViewModel.user.collect { user ->
                    if (user != null) {
                        tvDisplayName.text = user.displayName ?: user.username ?: "User"
                        if (!user.avatarUrl.isNullOrEmpty()) {
                            Glide.with(requireContext())
                                .load(user.avatarUrl)
                                .circleCrop()
                                .into(ivProfileAvatar)
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                libraryViewModel.playlists.collect { playlists ->
                    val models =
                        playlists.map { LibraryModel(it.playlistId, it.name, "Playlist") }
                    displayList.clear()
                    displayList.addAll(models)
                    libraryAdapter.notifyDataSetChanged()
                }
            }
        }

        val btnEditProfile = view.findViewById<TextView>(R.id.btnEditProfile)
        btnEditProfile.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, EditProfileFragment())
                .addToBackStack(null)
                .commit()
        }

        return view
    }
}