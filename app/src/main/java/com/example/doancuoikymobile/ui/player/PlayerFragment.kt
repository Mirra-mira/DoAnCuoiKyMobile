package com.example.doancuoikymobile.ui.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.doancuoikymobile.R

private const val ARG_TITLE = "song_title"

class PlayerFragment : Fragment() {

    private var songTitle: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Lấy tiêu đề bài hát được truyền từ PlaylistDetailFragment
        arguments?.let {
            songTitle = it.getString(ARG_TITLE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_player, container, false)

        // Cập nhật tiêu đề
        view.findViewById<TextView>(R.id.tvFullPlayerTitle)?.text = songTitle

        // Xử lý nút Close/Back để quay lại màn hình trước đó
        view.findViewById<ImageView>(R.id.btnClosePlayer)?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        val btnPlay = view.findViewById<ImageView>(R.id.btnMainPlay)
        var isPlaying = btnPlay.isSelected


        btnPlay.setOnClickListener {
            isPlaying = !isPlaying
            btnPlay.isSelected = isPlaying
        }


        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(title: String) =
            PlayerFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TITLE, title)
                }
            }
    }
}