package com.example.doancuoikymobile.ui.playlist

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
import com.example.doancuoikymobile.R
import com.example.doancuoikymobile.adapter.LibraryAdapter
import com.example.doancuoikymobile.adapter.LibraryModel
import com.example.doancuoikymobile.player.PlayerManager
import com.example.doancuoikymobile.ui.activity.MainActivity
import com.example.doancuoikymobile.ui.player.PlayerFragment
import com.example.doancuoikymobile.viewmodel.PlaylistDetailViewModel
import com.example.doancuoikymobile.ui.dialog.ChoosePlaylistDialog
import com.example.doancuoikymobile.utils.EmptyStateHelper
import com.example.doancuoikymobile.viewmodel.PlayerViewModel
import com.example.doancuoikymobile.adapter.ItemType
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

private const val ARG_ID = "playlist_id"
private const val ARG_TITLE = "playlist_title"

class PlaylistDetailFragment : Fragment() {

    private var playlistId: String? = null
    private var playlistTitle: String? = null
    private val viewModel: PlaylistDetailViewModel by viewModels()
    private val playerViewModel: PlayerViewModel by activityViewModels()
    private val libraryViewModel: com.example.doancuoikymobile.viewmodel.LibraryViewModel by activityViewModels()
    private var displayList = ArrayList<LibraryModel>()
    private lateinit var libraryAdapter: LibraryAdapter
    private lateinit var emptyStateView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            playlistId = it.getString(ARG_ID)
            playlistTitle = it.getString(ARG_TITLE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_playlist_detail, container, false)

        view.findViewById<ImageView>(R.id.btnDetailBack).setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        view.findViewById<TextView>(R.id.tvPlaylistTitle).text = playlistTitle ?: "Danh sách phát"

        val rvSongList = view.findViewById<RecyclerView>(R.id.rvSongList)
        emptyStateView = view.findViewById(R.id.emptyStatePlaylist)
        val btnPlayBig = view.findViewById<ImageView>(R.id.btnPlayBig)
        val ivCoverArt = view.findViewById<ImageView>(R.id.ivPlaylistCover)
        val tvPlaylistSubtitle = view.findViewById<TextView>(R.id.tvPlaylistSubtitle)
        val currentUser = FirebaseAuth.getInstance().currentUser

        // Load liked songs to show like status
        currentUser?.let { user ->
            libraryViewModel.loadLibraryData(user.uid)
        }

        val songListHandler: (LibraryModel) -> Unit = { model ->
            val songList = viewModel.songs.value
            val songToPlay = songList.find { it.songId == model.id }

            songToPlay?.let { song ->
                // CẬP NHẬT: Gửi danh sách vào PlayerViewModel
                val startIndex = songList.indexOfFirst { it.songId == song.songId }
                playerViewModel.setPlaylist(songList, startIndex)

                parentFragmentManager.beginTransaction()
                    .replace(R.id.frameLayout, PlayerFragment.newInstance(
                        song = song,
                        playlist = songList,
                        startIndex = startIndex
                    ))
                    .addToBackStack("PlaylistDetail")
                    .commit()
            }
        }

        libraryAdapter = LibraryAdapter(
            displayList,
            onItemClick = songListHandler,
            onAddClick = { model ->
                // Tìm đối tượng Song đầy đủ từ danh sách đang hiển thị
                val song = viewModel.songs.value.find { it.songId == model.id } ?: return@LibraryAdapter

                lifecycleScope.launch {
                    val playlists = viewModel.getUserPlaylists()
                    ChoosePlaylistDialog(requireContext(), playlists) { selectedPlaylist ->
                        // Truyền đối tượng song (đã có previewUrl) vào ViewModel
                        viewModel.addSongToPlaylist(selectedPlaylist.playlistId, song)
                    }.show()
                }
            },
            onLikeClick = { model ->
                currentUser?.let { user ->
                    libraryViewModel.toggleLikeSong(user.uid, model.id)
                }
            }
        )
        rvSongList.layoutManager = LinearLayoutManager(context)
        rvSongList.adapter = libraryAdapter

        playlistId?.let { 
            viewModel.loadPlaylistSongs(it)
            viewModel.loadPlaylistInfo(it)
        }

        // Setup play button
        // Tìm đến đoạn btnPlayBig.setOnClickListener trong onCreateView:
        btnPlayBig.setOnClickListener {
            val songs = viewModel.songs.value
            if (songs.isNotEmpty()) {
                // Gửi toàn bộ danh sách bảng xếp hạng vào Player
                playerViewModel.setPlaylist(songs, 0)

                parentFragmentManager.beginTransaction()
                    .replace(R.id.frameLayout, PlayerFragment.newInstance(
                        song = songs[0],
                        playlist = ArrayList(songs), // Đảm bảo truyền dưới dạng ArrayList
                        startIndex = 0
                    ))
                    .addToBackStack("PlaylistDetail")
                    .commit()
            }
        }

        // Observe playlist cover image
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.playlistCoverUrl.collect { coverUrl ->
                    coverUrl?.let { url ->
                        Glide.with(this@PlaylistDetailFragment)
                            .load(url)
                            .placeholder(R.drawable.ic_launcher_background)
                            .into(ivCoverArt)
                    }
                }
            }
        }

        // Observe playlist track count
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.playlistTrackCount.collect { count ->
                    if (count > 0) {
                        tvPlaylistSubtitle.text = "$count songs"
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                if (currentUser != null) {
                    // Combine songs and liked song IDs
                    combine(
                        viewModel.songs,
                        libraryViewModel.likedSongIds
                    ) { songs, likedIds ->
                        songs.map { song ->
                            LibraryModel(
                                id = song.songId,
                                title = song.title,
                                subtitle = song.artistName ?: "",
                                imageUrl = song.coverUrl,
                                type = ItemType.SONG,
                                isLiked = song.songId in likedIds
                            )
                        }
                    }.collect { models ->
                        displayList.clear()
                        displayList.addAll(models)
                        libraryAdapter.notifyDataSetChanged()
                        
                        // Update track count if not from Deezer
                        if (viewModel.playlistTrackCount.value == 0 && models.isNotEmpty()) {
                            tvPlaylistSubtitle.text = "${models.size} songs"
                        }
                        
                        // Handle empty state
                        EmptyStateHelper.handleEmptyState(emptyStateView, rvSongList, models.isEmpty())
                        if (models.isEmpty()) {
                            EmptyStateHelper.updateEmptyState(
                                emptyStateView,
                                iconResId = R.drawable.ic_music_note,
                                title = "No Songs",
                                message = "Add songs to this playlist to get started"
                            )
                        }
                    }
                } else {
                    // No user logged in, just show songs without like status
                    viewModel.songs.collect { songList ->
                        val models = songList.map {
                            LibraryModel(
                                id = it.songId,
                                title = it.title,
                                subtitle = it.artistName ?: "",
                                imageUrl = it.coverUrl,
                                type = ItemType.SONG,
                                isLiked = false
                            )
                        }
                        displayList.clear()
                        displayList.addAll(models)
                        libraryAdapter.notifyDataSetChanged()
                        
                        // Update track count if not from Deezer
                        if (viewModel.playlistTrackCount.value == 0 && models.isNotEmpty()) {
                            tvPlaylistSubtitle.text = "${models.size} songs"
                        }
                        
                        // Handle empty state
                        EmptyStateHelper.handleEmptyState(emptyStateView, rvSongList, models.isEmpty())
                        if (models.isEmpty()) {
                            EmptyStateHelper.updateEmptyState(
                                emptyStateView,
                                iconResId = R.drawable.ic_music_note,
                                title = "No Songs",
                                message = "Add songs to this playlist to get started"
                            )
                        }
                    }
                }
            }
        }

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(id: String, title: String) =
            PlaylistDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_ID, id)
                    putString(ARG_TITLE, title)
                }
            }
    }
}