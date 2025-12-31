package com.example.doancuoikymobile.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.doancuoikymobile.R
import com.example.doancuoikymobile.repository.AuthRepository
import com.example.doancuoikymobile.ui.auth.AuthActivity
import com.google.android.material.materialswitch.MaterialSwitch

class SettingsFragment : Fragment() {

    private val authRepository = AuthRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        view.findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        val switchDataSaver = view.findViewById<MaterialSwitch>(R.id.switchDataSaver)
        switchDataSaver.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Toast.makeText(context, "Đã bật Tiết kiệm dữ liệu", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Đã tắt Tiết kiệm dữ liệu", Toast.LENGTH_SHORT).show()
            }
        }

        val settingsTopBar = view.findViewById<View>(R.id.settingsTopBar)
        val parentView = settingsTopBar?.parent as? ViewGroup
        val scrollView = parentView?.getChildAt(1) as? android.widget.ScrollView
        val linearLayout = scrollView?.getChildAt(0) as? android.widget.LinearLayout
        val btnLogout = Button(requireContext()).apply {
            text = "Đăng xuất"
            layoutParams = android.widget.LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 24, 16, 16)
            }
            setOnClickListener {
                authRepository.signOut()
                val intent = Intent(requireContext(), AuthActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                activity?.finish()
            }
        }
        linearLayout?.addView(btnLogout)

        return view
    }
}