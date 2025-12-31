package com.example.doancuoikymobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doancuoikymobile.model.User
import com.example.doancuoikymobile.repository.AuthRepository
import com.example.doancuoikymobile.repository.UserRepository
import com.example.doancuoikymobile.data.remote.firebase.UserRemoteDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val userRepository: UserRepository = UserRepository(UserRemoteDataSource())
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _updateStatus = MutableStateFlow<String>("")
    val updateStatus: StateFlow<String> = _updateStatus

    fun loadUser() {
        viewModelScope.launch {
            val firebaseUser = authRepository.getCurrentUser()
            if (firebaseUser != null) {
                userRepository.watchUser(firebaseUser.uid).collect { user ->
                    _user.value = user
                }
            }
        }
    }

    fun watchUser(userId: String): Flow<User?> {
        return userRepository.watchUser(userId)
    }

    fun updateProfile(displayName: String?, avatarUrl: String?) {
        viewModelScope.launch {
            val firebaseUser = authRepository.getCurrentUser()
            if (firebaseUser != null) {
                val currentUser = _user.value ?: User(userId = firebaseUser.uid)
                val updatedUser = currentUser.copy(
                    displayName = displayName?.takeIf { it.isNotEmpty() } ?: currentUser.displayName,
                    avatarUrl = avatarUrl?.takeIf { it.isNotEmpty() } ?: currentUser.avatarUrl
                )
                try {
                    userRepository.upsertUser(updatedUser)
                    _user.value = updatedUser
                    _updateStatus.value = "Profile updated successfully"
                } catch (e: Exception) {
                    _updateStatus.value = "Failed to update: ${e.message}"
                }
            }
        }
    }
}

