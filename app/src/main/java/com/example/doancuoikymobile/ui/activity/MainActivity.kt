package com.example.doancuoikymobile.ui.activity

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.doancuoikymobile.R
import com.example.doancuoikymobile.databinding.ActivityMainBinding
import com.example.doancuoikymobile.player.PlayerManager
import com.example.doancuoikymobile.ui.home.HomeFragment
import com.example.doancuoikymobile.ui.playlist.LibraryFragment
import com.example.doancuoikymobile.ui.profile.ProfileFragment
import com.example.doancuoikymobile.ui.search.SearchFragment
import com.example.doancuoikymobile.ui.player.PlayerFragment
import com.example.doancuoikymobile.viewmodel.PlayerViewModel
import com.example.doancuoikymobile.model.Song
import kotlinx.coroutines.launch
import com.bumptech.glide.Glide

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private lateinit var miniPlayerBar: CardView
    private lateinit var tvMiniTitle: TextView
    private lateinit var tvMiniSubtitle: TextView
    private lateinit var btnMiniPlayPause: ImageView // Đổi tên cho khớp logic

    private val playerViewModel: PlayerViewModel by viewModels()
    private var isPlayerScreen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        PlayerManager.init(applicationContext)

        // Ánh xạ View từ card_mini_player.xml
        miniPlayerBar = findViewById(R.id.cardMiniPlayer)
        tvMiniTitle = miniPlayerBar.findViewById(R.id.tvMiniTitle)
        tvMiniSubtitle = miniPlayerBar.findViewById(R.id.tvMiniSubtitle)
        btnMiniPlayPause = miniPlayerBar.findViewById(R.id.btnPlayPause) // ID đúng từ XML bạn gửi

        observePlayer()

        miniPlayerBar.setOnClickListener {
            replaceFragment(PlayerFragment())
        }

        btnMiniPlayPause.setOnClickListener {
            playerViewModel.togglePlayPause()
        }

        if (savedInstanceState == null) replaceFragment(HomeFragment())
        setupBottomNav()
        supportFragmentManager.addOnBackStackChangedListener {
            val currentFragment = supportFragmentManager.findFragmentById(R.id.frameLayout)
            // Nếu Fragment hiện tại KHÔNG PHẢI là PlayerFragment, thì cập nhật lại isPlayerScreen
            isPlayerScreen = currentFragment is PlayerFragment
            updateMiniPlayerVisibility()
        }
    }
    private fun updateMiniPlayerVisibility() {
        val currentSong = playerViewModel.currentSong.value
        if (currentSong != null && !isPlayerScreen) {
            showMiniPlayer(currentSong)
        } else {
            miniPlayerBar.visibility = View.GONE
        }
    }

    private fun observePlayer() {
        lifecycleScope.launch {
            playerViewModel.currentSong.collect { song ->
                // Mỗi khi bài hát thay đổi, kiểm tra để hiển thị
                updateMiniPlayerVisibility()
            }
        }

        lifecycleScope.launch {
            playerViewModel.isPlaying.collect { isPlaying ->
                btnMiniPlayPause.setImageResource(
                    if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
                )
            }
        }
    }

    private fun showMiniPlayer(song: Song) {
        miniPlayerBar.visibility = View.VISIBLE
        tvMiniTitle.text = song.title

        tvMiniSubtitle.text = song.artistName
            ?.takeIf { it.isNotBlank() }
            ?: "Unknown Artist"

        Glide.with(this)
            .load(song.coverUrl)
            .placeholder(R.drawable.ic_launcher_background)
            .error(R.drawable.ic_launcher_background)
            .into(miniPlayerBar.findViewById(R.id.imgThumbnail))
    }

    private fun setupBottomNav() {
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_nav_home -> replaceFragment(HomeFragment())
                R.id.bottom_nav_search -> replaceFragment(SearchFragment())
                R.id.bottom_nav_lib -> replaceFragment(LibraryFragment())
                R.id.bottom_nav_profile -> replaceFragment(ProfileFragment())
            }
            true
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        isPlayerScreen = fragment is PlayerFragment

        supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, fragment)
            .apply {
                // SỬA CHỖ NÀY: Để BackStack có tên để dễ quản lý
                if (isPlayerScreen) {
                    addToBackStack("Player")
                } else {
                    // Nếu chuyển sang các Tab chính thì nên xóa hết BackStack cũ
                    // để tránh bấm Back nhiều lần mới thoát được app
                    supportFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
                }
            }
            .commit()

        // Luôn gọi update sau khi commit
        updateMiniPlayerVisibility()
    }
}