package com.example.doancuoikymobile.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.doancuoikymobile.databinding.FragmentLoginBinding
import com.example.doancuoikymobile.repository.Status
import com.example.doancuoikymobile.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels()
    private lateinit var googleSignInClient: GoogleSignInClient

    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleGoogleSignInResult(task)
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupGoogleSignIn()
        setupClickListeners()
        observeAuthState()
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("428113886094-dv6bjtp9i7s9ppluifku5jbjt1a6oej6.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }

    private fun setupClickListeners() = with(binding) {

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.signIn(email, password)
        }

        btnGoogleSignIn.setOnClickListener {
            googleSignInLauncher.launch(googleSignInClient.signInIntent)
        }

        btnGoToRegister.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(com.example.doancuoikymobile.R.id.authContainer, RegisterFragment())
                .addToBackStack(null)
                .commit()
        }

        btnForgotPassword.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(com.example.doancuoikymobile.R.id.authContainer, ForgotPasswordFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun observeAuthState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.authState.collect { resource ->
                    when (resource?.status) {

                        Status.LOADING -> {
                            // optional: show loading
                        }

                        Status.SUCCESS -> {
                            val firebaseUser = resource.data ?: return@collect

                            lifecycleScope.launch {
                                val userRepository =
                                    com.example.doancuoikymobile.repository.UserRepository(
                                        com.example.doancuoikymobile.data.remote.firebase.UserRemoteDataSource()
                                    )
                                userRepository.handleGoogleSignIn(firebaseUser)
                            }

                            viewModel.clearState()
                            (activity as? AuthActivity)?.navigateToMain()
                        }

                        Status.ERROR -> {
                            Toast.makeText(
                                context,
                                "Lỗi: ${resource.message}",
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

    private fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken

            if (idToken != null) {
                viewModel.signInWithGoogle(idToken)
            } else {
                Toast.makeText(context, "Không lấy được ID Token", Toast.LENGTH_SHORT).show()
            }
        } catch (e: ApiException) {
            Toast.makeText(context, "Google Sign-In lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
