package com.example.doancuoikymobile.ui.artist

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
import com.example.doancuoikymobile.player.PlayerManager
import com.example.doancuoikymobile.ui.player.PlayerFragment
import com.example.doancuoikymobile.viewmodel.ArtistDetailViewModel
import com.example.doancuoikymobile.utils.EmptyStateHelper
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

private const val ARG_ARTIST_ID = "artist_id"

class ArtistDetailFragment : Fragment() {

    private var artistId: String? = null
    private val viewModel: ArtistDetailViewModel by viewModels()
    private var displayList = ArrayList<LibraryModel>()
    private lateinit var libraryAdapter: LibraryAdapter
    private lateinit var emptyStateView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            artistId = it.getString(ARG_ARTIST_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_artist_detail, container, false)

        view.findViewById<ImageView>(R.id.btnDetailBack).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        val rvSongList = view.findViewById<RecyclerView>(R.id.rvSongList)
        emptyStateView = view.findViewById(R.id.emptyStateArtist)
        val btnFollow = view.findViewById<ImageView>(R.id.btnFollow)
        val btnPlayBig = view.findViewById<ImageView>(R.id.btnPlayBig)
        
        val currentUser = FirebaseAuth.getInstance().currentUser

        val songListHandler: (LibraryModel) -> Unit = { model ->
            val songToPlay = viewModel.songs.value.find { it.songId == model.id }
            songToPlay?.let { song ->
                PlayerManager.playSong(song)
                val songList = viewModel.songs.value
                val startIndex = songList.indexOfFirst { it.songId == song.songId }

                parentFragmentManager.beginTransaction()
                    .replace(
                        R.id.frameLayout,
                        PlayerFragment.newInstance(
                            song = song,
                            playlist = songList,
                            startIndex = startIndex
                        )
                    )
                    .addToBackStack("ArtistDetail")
                    .commit()
            }
        }

        libraryAdapter = LibraryAdapter(
            displayList,
            onItemClick = songListHandler,
            onAddClick = { /* TODO: Add to playlist */ }
        )
        rvSongList.layoutManager = LinearLayoutManager(context)
        rvSongList.adapter = libraryAdapter

        // Load artist data
        artistId?.let { 
            viewModel.loadArtist(it)
            currentUser?.let { user ->
                viewModel.checkFollowStatus(user.uid, it)
            }
        }

        // Setup follow button
        btnFollow.setOnClickListener {
            val artist = viewModel.artist.value
            currentUser?.let { user ->
                artist?.let {
                    viewModel.toggleFollow(user.uid, it)
                }
            }
        }

        // Setup play button
        btnPlayBig.setOnClickListener {
            val songs = viewModel.songs.value
            if (songs.isNotEmpty()) {
                PlayerManager.playSong(songs[0])
                parentFragmentManager.beginTransaction()
                    .replace(
                        R.id.frameLayout,
                        PlayerFragment.newInstance(
                            song = songs[0],
                            playlist = songs,
                            startIndex = 0
                        )
                    )
                    .addToBackStack("ArtistDetail")
                    .commit()
            }
        }

        // Observe artist data
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.artist.collect { artist ->
                    artist?.let {
                        view.findViewById<TextView>(R.id.tvArtistTitle).text = it.name
                        val followersText = if (it.followers > 0) {
                            "${it.followers} followers"
                        } else {
                            "Artist"
                        }
                        view.findViewById<TextView>(R.id.tvArtistSubtitle).text = followersText

                        // Load artist image
                        it.pictureUrl?.let { imageUrl ->
                            Glide.with(this@ArtistDetailFragment)
                                .load(imageUrl)
                                .placeholder(R.drawable.ic_launcher_background)
                                .into(view.findViewById(R.id.ivArtistImage))
                        }
                    }
                }
            }
        }

        // Observe follow status
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isFollowed.collect { isFollowed ->
                    btnFollow.setImageResource(
                        if (isFollowed) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
                    )
                }
            }
        }

        // Observe songs
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.songs.collect { songList ->
                    val models = songList.map {
                        LibraryModel(it.songId, it.title, it.artistName ?: "")
                    }
                    displayList.clear()
                    displayList.addAll(models)
                    libraryAdapter.notifyDataSetChanged()

                    // Handle empty state
                    EmptyStateHelper.handleEmptyState(emptyStateView, rvSongList, songList.isEmpty())
                    if (songList.isEmpty()) {
                        EmptyStateHelper.updateEmptyState(
                            emptyStateView,
                            iconResId = R.drawable.ic_music_note,
                            title = "No Songs",
                            message = "No tracks available for this artist"
                        )
                    }
                }
            }
        }

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(artistId: String) =
            ArtistDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_ARTIST_ID, artistId)
                }
            }
    }
}

