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
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.doancuoikymobile.R
import com.example.doancuoikymobile.ui.theme.DoAnCuoiKyMobileTheme
import com.example.doancuoikymobile.utils.NavigationHelper
import com.example.doancuoikymobile.viewmodel.SearchViewModel
import android.content.Context

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class SearchFragment : Fragment() {
    private val viewModel: SearchViewModel by viewModels {
        SearchViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_search, container, false)
        val composeView = view.findViewById<ComposeView>(R.id.composeView)

        composeView.apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )

            setContent {
                DoAnCuoiKyMobileTheme(dynamicColor = false) {
                    SearchScreen(
                        viewModel = viewModel,
                        onSongClick = { song ->
                            NavigationHelper.openPlayer(this@SearchFragment, song)
                        },
                        onPlaylistClick = { title, subtitle ->
                            NavigationHelper.openPlaylist(this@SearchFragment, title, subtitle)
                        }
                    )
                }
            }
            return view
        }
    }
}

class SearchViewModelFactory(private val context: Context) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SearchViewModel(context = context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

