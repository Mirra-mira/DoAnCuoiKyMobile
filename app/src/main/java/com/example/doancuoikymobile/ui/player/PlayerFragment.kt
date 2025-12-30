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
import com.example.doancuoikymobile.R
import com.example.doancuoikymobile.viewmodel.PlayerViewModel
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.launch

class PlayerFragment : Fragment() {

    private val viewModel: PlayerViewModel by viewModels()
    private lateinit var btnPlay: ImageView
    private lateinit var progressBar: LinearProgressIndicator
    private lateinit var tvTitle: TextView
    private lateinit var tvArtist: TextView
    private lateinit var tvCurrentTime: TextView
    private lateinit var tvTotalTime: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_player, container, false)

        // Ánh xạ View đúng theo XML bạn đã gửi
        tvTitle = view.findViewById(R.id.tvFullPlayerTitle)
        tvArtist = view.findViewById(R.id.tvArtist) // ID từ XML của bạn
        btnPlay = view.findViewById(R.id.btnMainPlay)
        progressBar = view.findViewById(R.id.progressBar) // ID từ XML của bạn
        tvCurrentTime = view.findViewById(R.id.tvCurrentTime)
        tvTotalTime = view.findViewById(R.id.tvTotalTime)

        view.findViewById<ImageView>(R.id.btnClosePlayer)?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        observeViewModel()

        btnPlay.setOnClickListener {
            viewModel.togglePlayPause()
        }

        return view
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Quan sát bài hát hiện tại từ ViewModel
            launch {
                viewModel.currentSong.collect { song ->
                    song?.let {
                        tvTitle.text = it.title
                        // Vì model Song không có artistName, tạm để trống hoặc ID
                        tvArtist.text = it.mainArtistId ?: ""
                    }
                }
            }
            // Trạng thái Play/Pause
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
        }
    }

    private fun formatTime(ms: Long): String {
        val minutes = (ms / 1000) / 60
        val seconds = (ms / 1000) % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    companion object {
        @JvmStatic
        fun newInstance(title: String) = PlayerFragment()
    }
}