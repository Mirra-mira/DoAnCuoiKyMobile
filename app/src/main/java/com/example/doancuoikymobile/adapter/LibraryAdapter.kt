package com.example.doancuoikymobile.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.doancuoikymobile.R

enum class ItemType { SONG, ARTIST, PLAYLIST }

data class LibraryModel(
    val id: String = "",
    val title: String,
    val subtitle: String,
    val imageUrl: String? = null,
    val type: ItemType,
    val isLiked: Boolean = false
)

class LibraryAdapter(
    private val dataList: List<LibraryModel>,
    private val onItemClick: (LibraryModel) -> Unit,
    private val onAddClick: (LibraryModel) -> Unit,
    private val onLikeClick: ((LibraryModel) -> Unit)? = null
) : RecyclerView.Adapter<LibraryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvItemTitle)
        val tvSubtitle: TextView = view.findViewById(R.id.tvItemSubtitle)
        val btnLike: ImageView? = view.findViewById(R.id.btnLikeSong)
        val btnAdd: View = view.findViewById(R.id.btnAddToPlaylist)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_library, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataList[position]
        holder.tvTitle.text = item.title
        holder.tvSubtitle.text = item.subtitle

        holder.btnAdd.visibility = if (item.type == ItemType.SONG) View.VISIBLE else View.GONE

        holder.btnLike?.let {
            val icon = if (item.isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
            it.setImageResource(icon)
            it.visibility = if (item.type == ItemType.PLAYLIST) View.GONE else View.VISIBLE
        }

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }

        holder.btnLike?.setOnClickListener {
            onLikeClick?.invoke(item)
        }

        holder.btnAdd.setOnClickListener {
            onAddClick(item)
        }
    }

    override fun getItemCount() = dataList.size
}