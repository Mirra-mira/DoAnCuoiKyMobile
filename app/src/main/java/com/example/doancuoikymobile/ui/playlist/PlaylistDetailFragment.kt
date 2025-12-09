package com.example.doancuoikymobile.ui.playlist

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
import com.example.doancuoikymobile.ui.activity.MainActivity
import java.util.* // Dùng cho dữ liệu giả lập
import com.example.doancuoikymobile.ui.player.PlayerFragment // Add this line
private const val ARG_TITLE = "playlist_title"
private const val ARG_SUBTITLE = "playlist_subtitle"

class PlaylistDetailFragment : Fragment() {

    private var playlistTitle: String? = null
    private var playlistSubtitle: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Lấy dữ liệu được truyền từ LibraryFragment
        arguments?.let {
            playlistTitle = it.getString(ARG_TITLE)
            playlistSubtitle = it.getString(ARG_SUBTITLE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_playlist_detail, container, false)

        // 1. Xử lý nút Back
        view.findViewById<ImageView>(R.id.btnDetailBack).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // 2. Cập nhật tiêu đề dựa trên dữ liệu nhận được
        view.findViewById<TextView>(R.id.tvPlaylistTitle).text = playlistTitle ?: "N/A"
        view.findViewById<TextView>(R.id.tvPlaylistSubtitle).text = playlistSubtitle ?: "N/A"

        // 3. Setup Danh sách Bài hát (RecyclerView)
        val rvSongList = view.findViewById<RecyclerView>(R.id.rvSongList)

        // Tạo dữ liệu giả cho danh sách bài hát trong Playlist này
        val songData = createDummySongData()

        // TÁI SỬ DỤNG Adapter cũ, nhưng click handler lúc này có thể là rỗng (hoặc logic Play nhạc)
        val songListHandler: (LibraryModel) -> Unit = { song ->

            // 1. CHUYỂN SANG MÀN HÌNH PLAYER CHÍNH
            val playerFragment = PlayerFragment.newInstance(song.title)

            parentFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, playerFragment)
                .addToBackStack("PlaylistDetail")
                .commit()

            // 2. HIỂN THỊ MINI PLAYER BAR TRÊN ACTIVITY
            val activity = requireActivity() as? MainActivity // Giả sử bạn có MainActivity
            activity?.showMiniPlayer(song.title, song.subtitle) // Gọi hàm trong Activity để hiện Mini Player
        }

        rvSongList.layoutManager = LinearLayoutManager(context)
        // Dùng LibraryAdapter và truyền danh sách bài hát (songData)
        rvSongList.adapter = LibraryAdapter(songData, songListHandler)

        return view
    }

    // Tạo dữ liệu bài hát giả
    private fun createDummySongData(): List<LibraryModel> {
        val random = Random()
        return listOf(
            LibraryModel("Bài 1 - ${playlistTitle}", "Ca sĩ A"),
            LibraryModel("Bài 2 - ${playlistTitle}", "Ca sĩ B"),
            LibraryModel("Bài 3 - ${playlistTitle}", "Ca sĩ C"),
            LibraryModel("Bài 4 - ${playlistTitle}", "Ca sĩ D"),
            LibraryModel("Bài 5 - ${playlistTitle}", "Ca sĩ E"),
            LibraryModel("Bài 6 - ${playlistTitle}", "Ca sĩ F"),
            LibraryModel("Bài 7 - ${playlistTitle}", "Ca sĩ G")
        )
    }

    companion object {
        @JvmStatic
        fun newInstance(title: String, subtitle: String) =
            PlaylistDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TITLE, title)
                    putString(ARG_SUBTITLE, subtitle)
                }
            }
    }
}