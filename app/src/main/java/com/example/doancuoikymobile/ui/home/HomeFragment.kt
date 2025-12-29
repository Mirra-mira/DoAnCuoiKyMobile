package com.example.doancuoikymobile.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView // 1. Import ImageView
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.example.doancuoikymobile.R
import com.example.doancuoikymobile.ui.settings.SettingsFragment // 2. Import SettingsFragment
import com.example.doancuoikymobile.ui.theme.DoAnCuoiKyMobileTheme
import com.example.doancuoikymobile.utils.NavigationHelper

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 1. Inflate giao diện XML
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // 2. Xử lý phần Jetpack Compose (HomeScreen)
        val composeView = view.findViewById<ComposeView>(R.id.HomeScreenComposeView)
        composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                DoAnCuoiKyMobileTheme {
                    HomeScreen(
                        username = "Username",
                        isNewUser = false,
                        onSongClick = { title ->
                            NavigationHelper.openPlayer(this@HomeFragment, title)
                        },
                        onPlaylistClick = { title, subtitle ->
                            NavigationHelper.openPlaylist(this@HomeFragment, title, "Playlist")
                        }
                    )
                }
            }
        }

        // 3. Xử lý phần View XML (Nút Cài đặt) - ĐÂY LÀ PHẦN BẠN CẦN THÊM
        val btnSettings = view.findViewById<ImageView>(R.id.home_settings)
        btnSettings.setOnClickListener {
            // Chuyển sang SettingsFragment
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    android.R.anim.slide_in_left,
                    android.R.anim.slide_out_right,
                    android.R.anim.slide_in_left,
                    android.R.anim.slide_out_right
                )
                .replace(R.id.frameLayout, SettingsFragment()) // Đảm bảo ID container trong ActivityMain là frameLayout
                .addToBackStack(null) // Để user ấn Back quay lại được Home
                .commit()
        }

        // 4. Trả về view tổng (Đưa return ra ngoài cùng)
        return view
    }
}