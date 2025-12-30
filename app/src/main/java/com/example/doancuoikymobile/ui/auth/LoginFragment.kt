package com.example.doancuoikymobile.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.doancuoikymobile.R
import com.example.doancuoikymobile.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)
        val btnLogin = view.findViewById<Button>(R.id.btnLogin)

        // Điều hướng Fragment
        view.findViewById<TextView>(R.id.btnGoToRegister).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.authContainer, RegisterFragment())
                .addToBackStack(null)
                .commit()
        }

        view.findViewById<TextView>(R.id.btnForgotPassword).setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.authContainer, ForgotPasswordFragment())
                .addToBackStack(null)
                .commit()
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass = etPassword.text.toString().trim()
            if (email.isNotEmpty() && pass.isNotEmpty()) {
                viewModel.signIn(email, pass)
            } else {
                Toast.makeText(context, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.authState.collect { result ->
                    result?.onSuccess {
                        viewModel.clearState()
                        (activity as? AuthActivity)?.navigateToMain()
                    }?.onFailure { error ->
                        Toast.makeText(context, "Lỗi: ${error.message}", Toast.LENGTH_SHORT).show()
                        viewModel.clearState()
                    }
                }
            }
        }

        return view
    }
}