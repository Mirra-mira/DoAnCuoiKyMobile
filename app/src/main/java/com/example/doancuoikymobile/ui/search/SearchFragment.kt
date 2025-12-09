package com.example.doancuoikymobile.ui.search

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.example.doancuoikymobile.R
import com.example.doancuoikymobile.ui.theme.DoAnCuoiKyMobileTheme
import com.example.doancuoikymobile.utils.NavigationHelper

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SearchFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SearchFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate XML layout
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        // Tìm ComposeView từ XML
        val composeView = view.findViewById<ComposeView>(R.id.composeView)

        // Setup Compose
        composeView.apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )

            setContent {
                DoAnCuoiKyMobileTheme(dynamicColor = false) {
                    SearchScreen(
                        onSongClick = { songTitle ->
                            NavigationHelper.openPlayer(this@SearchFragment, songTitle)
                        }

                    )
                }
            }

            return view
        }
    }
}

