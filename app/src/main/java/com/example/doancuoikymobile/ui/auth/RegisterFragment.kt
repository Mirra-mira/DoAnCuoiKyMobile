package com.example.doancuoikymobile.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.doancuoikymobile.R

class RegisterFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_register, container, false)

        // Quay lại Login
        view.findViewById<TextView>(R.id.btnBackToLogin).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Đăng ký xong -> Vào Main (hoặc về Login tuỳ bạn)
        view.findViewById<Button>(R.id.btnRegister).setOnClickListener {
            (activity as? AuthActivity)?.navigateToMain()
        }

        return view
    }
}