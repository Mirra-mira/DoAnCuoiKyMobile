package com.example.doancuoikymobile.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.example.doancuoikymobile.R
import com.example.doancuoikymobile.ui.auth.AuthActivity

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        // 1. Xử lý nút Back (Quay lại Home)
        view.findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // 2. Xử lý nút Đăng xuất
        view.findViewById<Button>(R.id.btnLogout).setOnClickListener {
            // Thực hiện logout (xóa token, clear data nếu có...)

            // Chuyển về màn hình AuthActivity (Đăng nhập)
            val intent = Intent(requireContext(), AuthActivity::class.java)
            // Xóa hết các Activity cũ trong Stack để user không Back lại được
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        return view
    }
}