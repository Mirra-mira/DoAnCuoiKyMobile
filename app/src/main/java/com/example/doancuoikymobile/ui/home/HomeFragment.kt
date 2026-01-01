package com.example.doancuoikymobile.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import com.example.doancuoikymobile.R
import com.example.doancuoikymobile.ui.theme.DoAnCuoiKyMobileTheme
import com.example.doancuoikymobile.utils.NavigationHelper
import com.example.doancuoikymobile.viewmodel.HomeViewModel

class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val composeView = view.findViewById<ComposeView>(R.id.HomeScreenComposeView)
        composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                DoAnCuoiKyMobileTheme {
                    val sectionsState by viewModel.sections.collectAsState()

                    HomeScreen(
                        sections = sectionsState,
                        onSongClick = { song ->
                            NavigationHelper.openPlayer(this@HomeFragment, song)
                        },
                        onPlaylistClick = { title, subtitle ->
                            NavigationHelper.openPlaylist(this@HomeFragment, title, subtitle)
                        }
                    )
                }
            }
        }

        view.findViewById<ImageView>(R.id.home_settings).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, com.example.doancuoikymobile.ui.settings.SettingsFragment())
                .addToBackStack(null)
                .commit()
        }

        return view
    }
}