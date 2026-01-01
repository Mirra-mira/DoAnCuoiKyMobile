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
import kotlinx.coroutines.launch

private const val ARG_ID = "playlist_id"
private const val ARG_TITLE = "playlist_title"

class PlaylistDetailFragment : Fragment() {

    private var playlistId: String? = null
    private var playlistTitle: String? = null
    private val viewModel: PlaylistDetailViewModel by viewModels()
    private var displayList = ArrayList<LibraryModel>()
    private lateinit var libraryAdapter: LibraryAdapter

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

        val songListHandler: (LibraryModel) -> Unit = { model ->
            val songToPlay = viewModel.songs.value.find { it.songId == model.id }
            songToPlay?.let { song ->
                PlayerManager.playSong(song)
                (requireActivity() as? MainActivity)?.showMiniPlayer(song.title, song.mainArtistId)

                parentFragmentManager.beginTransaction()
                    .replace(R.id.frameLayout, PlayerFragment.newInstance(song))
                    .addToBackStack("PlaylistDetail")
                    .commit()
            }
        }

        libraryAdapter = LibraryAdapter(displayList, songListHandler)
        rvSongList.layoutManager = LinearLayoutManager(context)
        rvSongList.adapter = libraryAdapter

        playlistId?.let { viewModel.loadPlaylistSongs(it) }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.songs.collect { songList ->
                    val models = songList.map {
                        // Map title và dùng mainArtistId làm subtitle cho LibraryModel
                        LibraryModel(it.songId, it.title, it.mainArtistId ?: "")
                    }
                    displayList.clear()
                    displayList.addAll(models)
                    libraryAdapter.notifyDataSetChanged()
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