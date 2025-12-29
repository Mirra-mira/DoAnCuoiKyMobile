package com.example.doancuoikymobile.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.doancuoikymobile.R

class LoginFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        // 1. Chuyển sang Register
        view.findViewById<TextView>(R.id.btnGoToRegister).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.authContainer, RegisterFragment())
                .addToBackStack(null)
                .commit()
        }

        // 2. Chuyển sang Forgot Password
        view.findViewById<TextView>(R.id.btnForgotPassword).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.authContainer, ForgotPasswordFragment())
                .addToBackStack(null)
                .commit()
        }

        // 3. Đăng nhập thành công -> Vào Main Activity
        view.findViewById<Button>(R.id.btnLogin).setOnClickListener {
            (activity as? AuthActivity)?.navigateToMain()
        }

        return view
    }
}