package com.example.doancuoikymobile.utils

import androidx.fragment.app.Fragment
import com.example.doancuoikymobile.R
import com.example.doancuoikymobile.ui.player.PlayerFragment
import com.example.doancuoikymobile.ui.playlist.PlaylistDetailFragment

object NavigationHelper {
    fun openPlayer(fromFragment: Fragment, songTitle: String) {
        val playerFragment = PlayerFragment.newInstance(songTitle)

        val activity = fromFragment.requireActivity()
        activity.supportFragmentManager
            .beginTransaction()
            .replace(R.id.frameLayout, playerFragment)
            .addToBackStack("player")
            .commit()
    }

    fun openPlaylist(fromFragment: Fragment, playlistTitle: String, playlistSubtitle : String) {
        val playerFragment = PlaylistDetailFragment.newInstance(playlistTitle, playlistSubtitle)

        val activity = fromFragment.requireActivity()
        activity.supportFragmentManager
            .beginTransaction()
            .replace(R.id.frameLayout, playerFragment)
            .addToBackStack("playlist_detail")
            .commit()
    }
}