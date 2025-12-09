package com.example.doancuoikymobile.utils

import androidx.fragment.app.Fragment
import com.example.doancuoikymobile.R
import com.example.doancuoikymobile.ui.player.PlayerFragment

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
}