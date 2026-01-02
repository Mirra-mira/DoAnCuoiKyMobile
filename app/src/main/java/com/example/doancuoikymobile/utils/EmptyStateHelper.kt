package com.example.doancuoikymobile.utils

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.example.doancuoikymobile.R

object EmptyStateHelper {
    /**
     * Hiển thị hoặc ẩn empty state dựa trên trạng thái danh sách
     * @param emptyStateView: View chứa empty state layout
     * @param recyclerView: RecyclerView hiển thị dữ liệu
     * @param isEmpty: True nếu danh sách trống
     */
    fun handleEmptyState(
        emptyStateView: View,
        recyclerView: View,
        isEmpty: Boolean
    ) {
        if (isEmpty) {
            emptyStateView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyStateView.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    /**
     * Cập nhật nội dung empty state
     */
    fun updateEmptyState(
        emptyStateView: View,
        iconResId: Int = android.R.drawable.ic_menu_info_details,
        title: String = "No Data",
        message: String = "No items found",
        actionButtonText: String? = null,
        onActionClick: (() -> Unit)? = null
    ) {
        val icon = emptyStateView.findViewById<ImageView>(R.id.empty_state_icon)
        val titleView = emptyStateView.findViewById<TextView>(R.id.empty_state_title)
        val messageView = emptyStateView.findViewById<TextView>(R.id.empty_state_message)
        val actionBtn = emptyStateView.findViewById<Button>(R.id.empty_state_action_btn)

        icon?.setImageResource(iconResId)
        titleView?.text = title
        messageView?.text = message

        if (actionButtonText != null && onActionClick != null) {
            actionBtn?.visibility = View.VISIBLE
            actionBtn?.text = actionButtonText
            actionBtn?.setOnClickListener { onActionClick() }
        } else {
            actionBtn?.visibility = View.GONE
        }
    }
}
