package com.example.doancuoikymobile.ui.dialog

import android.content.Context
import com.example.doancuoikymobile.model.Playlist
import android.app.Dialog
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import com.example.doancuoikymobile.R

class ChoosePlaylistDialog(
    context: Context,
    private val playlists: List<Playlist>,
    private val onSelect: (Playlist) -> Unit
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_choose_playlist)

        val container = findViewById<LinearLayout>(R.id.playlistContainer)

        playlists.forEach { playlist ->
            val tv = TextView(context).apply {
                text = playlist.name
                textSize = 16f
                setPadding(24, 24, 24, 24)
                setOnClickListener {
                    onSelect(playlist)
                    dismiss()
                }
            }
            container.addView(tv)
        }
    }
}
