package com.example.doancuoikymobile.data.local

import android.content.Context
import android.provider.MediaStore
import android.util.Log
import com.example.doancuoikymobile.model.Song

class LocalSongProvider(private val context: Context) {

    /**
     * Quét toàn bộ file audio trong bộ nhớ máy
     */
    fun getAllLocalSongs(): List<Song> {
        val songList = mutableListOf<Song>()

        // Truy vấn vào thư viện Media của Android
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

        val query = context.contentResolver.query(
            collection,
            projection,
            selection,
            null,
            "${MediaStore.Audio.Media.TITLE} ASC"
        )

        query?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn)
                val artist = cursor.getString(artistColumn)
                val duration = cursor.getInt(durationColumn) / 1000
                val path = cursor.getString(dataColumn)

                // Tạo URI chuẩn cho Media3/ExoPlayer từ ID
                val contentUri = "${MediaStore.Audio.Media.EXTERNAL_CONTENT_URI}/$id"

                songList.add(
                    Song(
                        songId = "local_$id",
                        title = title,
                        mainArtistId = artist,
                        duration = duration,
                        audioUrl = contentUri,
                        coverUrl = "",
                        isOnline = false // Đánh dấu là nhạc offline
                    )
                )
            }
        }

        Log.d("LOCAL_PROVIDER", "Found ${songList.size} local songs")
        return songList
    }
}