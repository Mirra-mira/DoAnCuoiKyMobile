package com.example.doancuoikymobile.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.example.doancuoikymobile.R

class ForgotPasswordFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_forgot_password, container, false)

        // Nút Back
        view.findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Nút Send (Gửi xong thì quay lại Login)
        view.findViewById<View>(R.id.btnSend).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        return view
    }
}