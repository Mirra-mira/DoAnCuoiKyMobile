package com.example.doancuoikymobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doancuoikymobile.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository = AuthRepository()) : ViewModel() {

    // Trạng thái xử lý: null (chưa làm gì), Success (thành công), Failure (lỗi)
    private val _authState = MutableStateFlow<Result<FirebaseUser>?>(null)
    val authState: StateFlow<Result<FirebaseUser>?> = _authState

    // Trạng thái Loading để hiển thị ProgressBar nếu cần
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun signIn(email: String, pass: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.signIn(email, pass)
            _authState.value = result
            _isLoading.value = false
        }
    }

    fun signUp(email: String, pass: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.signUp(email, pass)
            _authState.value = result
            _isLoading.value = false
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.signInWithGoogle(idToken)
            _authState.value = result
            _isLoading.value = false
        }
    }

    fun clearState() {
        _authState.value = null
    }
}