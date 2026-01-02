package com.example.doancuoikymobile.utils

import androidx.fragment.app.Fragment
import com.example.doancuoikymobile.R
import com.example.doancuoikymobile.model.Song
import com.example.doancuoikymobile.ui.player.PlayerFragment
import com.example.doancuoikymobile.ui.playlist.PlaylistDetailFragment
import com.example.doancuoikymobile.ui.artist.ArtistDetailFragment

object NavigationHelper {

    fun openPlayer(fromFragment: Fragment, song: Song) {
        val playerFragment = PlayerFragment.newInstance(
            song = song,
            playlist = listOf(song), // fallback playlist
            startIndex = 0
        )

        fromFragment.requireActivity()
            .supportFragmentManager
            .beginTransaction()
            .replace(R.id.frameLayout, playerFragment)
            .addToBackStack("player")
            .commit()
    }

    fun openPlaylist(
        fromFragment: Fragment,
        playlistId: String,
        playlistTitle: String
    ) {
        val fragment = PlaylistDetailFragment.newInstance(
            playlistId,
            playlistTitle
        )

        fromFragment.requireActivity()
            .supportFragmentManager
            .beginTransaction()
            .replace(R.id.frameLayout, fragment)
            .addToBackStack("playlist_detail")
            .commit()
    }

    fun openArtist(
        fromFragment: Fragment,
        artistId: String
    ) {
        val fragment = ArtistDetailFragment.newInstance(artistId)

        fromFragment.requireActivity()
            .supportFragmentManager
            .beginTransaction()
            .replace(R.id.frameLayout, fragment)
            .addToBackStack("artist_detail")
            .commit()
    }
}
