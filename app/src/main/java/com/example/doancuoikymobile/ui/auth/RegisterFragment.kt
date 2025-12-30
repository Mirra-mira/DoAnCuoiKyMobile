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

class RegisterFragment : Fragment() {

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_register, container, false)

        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)
        val btnRegister = view.findViewById<Button>(R.id.btnRegister)

        view.findViewById<TextView>(R.id.btnBackToLogin).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnRegister.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass = etPassword.text.toString().trim()
            if (email.isNotEmpty() && pass.isNotEmpty()) {
                viewModel.signUp(email, pass)
            }
        }

        // SỬA LỖI: Sử dụng repeatOnLifecycle thay cho launchWhenStarted
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.authState.collect { result ->
                    result?.onSuccess {
                        viewModel.clearState()
                        Toast.makeText(context, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
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