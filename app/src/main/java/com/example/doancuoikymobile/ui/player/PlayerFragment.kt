package com.example.doancuoikymobile.ui.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.doancuoikymobile.R
import com.example.doancuoikymobile.model.Song
import com.example.doancuoikymobile.player.PlayerManager
import com.example.doancuoikymobile.viewmodel.PlayerViewModel
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.launch

class PlayerFragment : Fragment() {

    private val viewModel: PlayerViewModel by viewModels()
    private lateinit var btnPlay: ImageView
    private lateinit var btnNext: ImageView
    private lateinit var btnPrev: ImageView
    private lateinit var btnShuffle: ImageView
    private lateinit var btnRepeat: ImageView
    private lateinit var progressBar: LinearProgressIndicator
    private lateinit var tvTitle: TextView
    private lateinit var tvArtist: TextView
    private lateinit var tvCurrentTime: TextView
    private lateinit var tvTotalTime: TextView
    private lateinit var imgPlayer: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_player, container, false)

        tvTitle = view.findViewById(R.id.tvFullPlayerTitle)
        tvArtist = view.findViewById(R.id.tvArtist)
        btnPlay = view.findViewById(R.id.btnMainPlay)
        btnNext = view.findViewById(R.id.btnNext)
        btnPrev = view.findViewById(R.id.btnPrevious)
        btnShuffle = view.findViewById(R.id.btnShuffle)
        btnRepeat = view.findViewById(R.id.btnRepeat)
        progressBar = view.findViewById(R.id.progressBar)
        tvCurrentTime = view.findViewById(R.id.tvCurrentTime)
        tvTotalTime = view.findViewById(R.id.tvTotalTime)
        imgPlayer = view.findViewById(R.id.imgPlayer)

        view.findViewById<ImageView>(R.id.btnClosePlayer)?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        PlayerManager.init(requireContext())

        val song = arguments?.getSerializable("song") as? Song
        val songs = arguments?.getSerializable("playlist") as? ArrayList<Song>
        val index = arguments?.getInt("startIndex") ?: 0

        if (!songs.isNullOrEmpty()) {
            viewModel.setPlaylist(songs, index)
        } else if (song != null) {
            viewModel.playSong(song)
        }

        observeViewModel()

        btnPlay.setOnClickListener {
            viewModel.togglePlayPause()
        }

        btnNext.setOnClickListener {
            viewModel.playNext()
        }

        btnPrev.setOnClickListener {
            viewModel.playPrevious()
        }

        btnShuffle.setOnClickListener {
            viewModel.toggleShuffle()
        }

        btnRepeat.setOnClickListener {
            viewModel.toggleRepeat()
        }

        return view
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            launch {
                viewModel.currentSong.collect { song ->
                    song?.let {
                        tvTitle.text = it.title
                        if (!it.coverUrl.isNullOrEmpty()) {
                            Glide.with(requireContext())
                                .load(it.coverUrl)
                                .into(imgPlayer)
                        }
                    }
                }
            }
            launch {
                viewModel.artistName.collect { name ->
                    tvArtist.text = name.ifEmpty { "Unknown Artist" }
                }
            }
            launch {
                viewModel.isPlaying.collect { isPlaying ->
                    btnPlay.setImageResource(
                        if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
                    )
                }
            }
            launch {
                viewModel.progress.collect { pos ->
                    progressBar.progress = pos.toInt()
                    tvCurrentTime.text = formatTime(pos)
                }
            }
            launch {
                viewModel.duration.collect { dur ->
                    if (dur > 0) {
                        progressBar.max = dur.toInt()
                        tvTotalTime.text = formatTime(dur)
                    }
                }
            }
            launch {
                viewModel.isShuffle.collect { shuffle ->
                    btnShuffle.alpha = if (shuffle) 1f else 0.5f
                }
            }
            launch {
                viewModel.repeatMode.collect { mode ->
                    btnRepeat.alpha = if (mode > 0) 1f else 0.5f
                }
            }
        }
    }

    private fun formatTime(ms: Long): String {
        val minutes = (ms / 1000) / 60
        val seconds = (ms / 1000) % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    companion object {
        @JvmStatic
        fun newInstance(
            song: Song,
            playlist: List<Song>,
            startIndex: Int
        ) = PlayerFragment().apply {
            arguments = Bundle().apply {
                putSerializable("song", song)
                putSerializable("playlist", ArrayList(playlist))
                putInt("startIndex", startIndex)
            }
        }
    }
}