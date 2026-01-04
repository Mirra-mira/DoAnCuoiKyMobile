package com.example.doancuoikymobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doancuoikymobile.data.remote.firebase.UserRemoteDataSource
import com.example.doancuoikymobile.model.User
import com.example.doancuoikymobile.repository.AuthRepository
import com.example.doancuoikymobile.repository.Resource
import com.example.doancuoikymobile.repository.UserRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val userRepository: UserRepository =
        UserRepository(UserRemoteDataSource())
) : ViewModel() {

    private val _authState = MutableStateFlow<Resource<FirebaseUser>?>(null)
    val authState: StateFlow<Resource<FirebaseUser>?> = _authState

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = Resource.loading(null)

            authRepository.signUp(email, password)
                .onSuccess { firebaseUser ->

                    val newUser = User(
                        userId = firebaseUser.uid,
                        email = email,
                        username = email.substringBefore("@"),
                        role = "user"
                    )

                    userRepository.upsertUser(newUser)

                    _authState.value = Resource.success(firebaseUser)
                }
                .onFailure { e ->
                    _authState.value = Resource.error(
                        e.message ?: "Đăng ký thất bại",
                        null
                    )
                }
        }
    }

    fun signIn(email: String, password: String) {
        executeAuth { authRepository.signIn(email, password) }
    }

    fun signInWithGoogle(idToken: String) {
        executeAuth { authRepository.signInWithGoogle(idToken) }
    }

    private fun executeAuth(
        action: suspend () -> Result<FirebaseUser>
    ) {
        viewModelScope.launch {
            _authState.value = Resource.loading(null)

            action()
                .onSuccess {
                    _authState.value = Resource.success(it)
                }
                .onFailure {
                    _authState.value = Resource.error(
                        it.message ?: "Authentication failed",
                        null
                    )
                }
        }
    }

    fun clearState() {
        _authState.value = null
    }
}
