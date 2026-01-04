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
import androidx.navigation.fragment.findNavController
import com.example.doancuoikymobile.R
import com.example.doancuoikymobile.repository.Status
import com.example.doancuoikymobile.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {

    private val viewModel: AuthViewModel by viewModels()

    private var registerName: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_register, container, false)

        val etName = view.findViewById<EditText>(R.id.etName)
        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = view.findViewById<EditText>(R.id.etConfirmPassword)
        val btnRegister = view.findViewById<Button>(R.id.btnRegister)
        val btnBackToLogin = view.findViewById<TextView>(R.id.btnBackToLogin)

        btnBackToLogin.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPass = etConfirmPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(context, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPass) {
                Toast.makeText(context, "Mật khẩu không trùng khớp", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registerName = name
            viewModel.signUp(email, password)
        }

        observeAuthState(btnRegister)

        return view
    }

    private fun observeAuthState(btnRegister: Button) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.authState.collect { resource ->
                    when (resource?.status) {

                        Status.LOADING -> {
                            btnRegister.isEnabled = false
                        }

                        Status.SUCCESS -> {
                            btnRegister.isEnabled = true

                            Toast.makeText(
                                context,
                                "Đăng ký thành công!",
                                Toast.LENGTH_SHORT
                            ).show()

                            viewModel.clearState()
                            parentFragmentManager.popBackStack()
                        }

                        Status.ERROR -> {
                            btnRegister.isEnabled = true

                            Toast.makeText(
                                context,
                                resource.message ?: "Đăng ký thất bại",
                                Toast.LENGTH_SHORT
                            ).show()

                            viewModel.clearState()
                        }

                        null -> Unit
                    }
                }
            }
        }
    }
}
