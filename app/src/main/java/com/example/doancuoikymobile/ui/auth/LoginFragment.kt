package com.example.doancuoikymobile.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.doancuoikymobile.R
import com.example.doancuoikymobile.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private val viewModel: AuthViewModel by viewModels()
    private lateinit var googleSignInClient: GoogleSignInClient

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        handleGoogleSignInResult(task)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("428113886094-dv6bjtp9i7s9ppluifku5jbjt1a6oej6.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)
        val btnLogin = view.findViewById<Button>(R.id.btnLogin)
        val btnGoogleSignIn = view.findViewById<Button>(R.id.btnGoogleSignIn)

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

        btnGoogleSignIn.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.authState.collect { result ->
                    result?.onSuccess { firebaseUser ->
                        viewLifecycleOwner.lifecycleScope.launch {
                            val userRepository = com.example.doancuoikymobile.repository.UserRepository(
                                com.example.doancuoikymobile.data.remote.firebase.UserRemoteDataSource()
                            )
                            userRepository.handleGoogleSignIn(firebaseUser)
                        }
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

    private fun handleGoogleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (idToken != null) {
                viewModel.signInWithGoogle(idToken)
            } else {
                Toast.makeText(context, "Không thể lấy ID token từ Google", Toast.LENGTH_SHORT).show()
            }
        } catch (e: ApiException) {
            Toast.makeText(context, "Lỗi đăng nhập Google: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}