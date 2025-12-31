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
        val cardAvatar = view.findViewById<androidx.cardview.widget.CardView>(R.id.cardAvatar)
        val avatarImageView = cardAvatar?.getChildAt(0) as? ImageView

        libraryAdapter = LibraryAdapter(displayList) { item ->
            val detailFragment = PlaylistDetailFragment.newInstance(item.id, item.title)
            parentFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, detailFragment)
                .addToBackStack(null)
                .commit()
        }

        rvProfile?.layoutManager = LinearLayoutManager(context)
        rvProfile?.adapter = libraryAdapter

        val firebaseUser = authRepository.getCurrentUser()
        if (firebaseUser != null) {
            profileViewModel.loadUser()
            libraryViewModel.loadLibraryData(firebaseUser.uid)
        }

        var nameTextView: TextView? = null
        var emailTextView: TextView? = null

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                profileViewModel.user.collect { user ->
                    user?.let {
                        if (avatarImageView != null && it.avatarUrl != null && it.avatarUrl.isNotEmpty()) {
                            Glide.with(requireContext())
                                .load(it.avatarUrl)
                                .into(avatarImageView)
                        }
                        val userName = it.displayName ?: it.username
                        val userEmail = it.email
                        if (nameTextView == null) {
                            nameTextView = TextView(requireContext()).apply {
                                text = userName
                                textSize = 20f
                                textAlignment = View.TEXT_ALIGNMENT_CENTER
                                setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), android.R.color.black))
                            }
                            val btnEditProfile = view.findViewById<TextView>(R.id.btnEditProfile)
                            val parent = btnEditProfile?.parent as? ViewGroup
                            val index = parent?.indexOfChild(btnEditProfile) ?: 0
                            val layoutParams = android.widget.LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            ).apply {
                                topMargin = 16
                            }
                            parent?.addView(nameTextView, index + 1, layoutParams)
                        } else {
                            nameTextView?.text = userName
                        }
                        if (emailTextView == null) {
                            emailTextView = TextView(requireContext()).apply {
                                text = userEmail
                                textSize = 14f
                                textAlignment = View.TEXT_ALIGNMENT_CENTER
                                setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), android.R.color.darker_gray))
                            }
                            val parent = nameTextView?.parent as? ViewGroup
                            val index = parent?.indexOfChild(nameTextView) ?: 0
                            val layoutParams = android.widget.LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            ).apply {
                                topMargin = 8
                            }
                            parent?.addView(emailTextView, index + 1, layoutParams)
                        } else {
                            emailTextView?.text = userEmail
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                libraryViewModel.playlists.collect { playlists ->
                    val models = playlists.map { LibraryModel(it.playlistId, it.name, "Playlist") }
                    displayList.clear()
                    displayList.addAll(models)
                    libraryAdapter.notifyDataSetChanged()
                }
            }
        }

        return view
    }
}