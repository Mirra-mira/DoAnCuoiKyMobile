package com.example.doancuoikymobile.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.doancuoikymobile.R
import com.google.android.material.materialswitch.MaterialSwitch

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

        // 2. Xử lý Switch Tiết kiệm dữ liệu (Ví dụ)
        val switchDataSaver = view.findViewById<MaterialSwitch>(R.id.switchDataSaver)
        switchDataSaver.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Toast.makeText(context, "Đã bật Tiết kiệm dữ liệu", Toast.LENGTH_SHORT).show()
                // TODO: Lưu trạng thái vào SharedPreferences
            } else {
                Toast.makeText(context, "Đã tắt Tiết kiệm dữ liệu", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }
}