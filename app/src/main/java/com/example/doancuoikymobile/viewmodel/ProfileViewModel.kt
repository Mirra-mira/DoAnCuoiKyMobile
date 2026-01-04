package com.example.doancuoikymobile.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doancuoikymobile.model.User
import com.example.doancuoikymobile.repository.AuthRepository
import com.example.doancuoikymobile.repository.UserRepository
import com.example.doancuoikymobile.data.remote.firebase.UserRemoteDataSource
import com.example.doancuoikymobile.repository.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val userRepository: UserRepository = UserRepository(UserRemoteDataSource())
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    // Khởi tạo là Idle hoặc Success(null) thay vì Loading ngay lập tức
    private val _updateStatus = MutableStateFlow<Resource<String>?>(null)
    val updateStatus: StateFlow<Resource<String>?> = _updateStatus

    fun loadUser() {
        viewModelScope.launch {
            val firebaseUser = authRepository.getCurrentUser() ?: return@launch
            // Sử dụng collect để lắng nghe thay đổi thời gian thực từ Firestore
            userRepository.watchUser(firebaseUser.uid).collect {
                _user.value = it
            }
        }
    }

    fun updateFullProfile(
        displayName: String,
        imageUri: Uri?,
        newPassword: String?
    ) {
        viewModelScope.launch {
            val firebaseUser = authRepository.getCurrentUser() ?: return@launch
            _updateStatus.value = Resource.loading(null)

            try {
                // Chuyển toàn bộ thao tác nặng sang IO Thread
                withContext(Dispatchers.IO) {
                    var finalAvatarUrl = _user.value?.avatarUrl

                    // 1. Xử lý upload ảnh (nếu có)
                    imageUri?.let { uri ->
                        val uploadedUrl = userRepository.uploadAvatar(firebaseUser.uid, uri)
                        if (uploadedUrl != null) {
                            finalAvatarUrl = uploadedUrl
                        }
                    }

                    // 2. Xử lý đổi mật khẩu (nếu có nhập)
                    if (!newPassword.isNullOrBlank()) {
                        authRepository.updatePassword(newPassword)
                    }

                    // 3. Cập nhật thông tin User vào Firestore
                    val updatedUser = (_user.value ?: User(userId = firebaseUser.uid)).copy(
                        displayName = displayName.ifBlank { _user.value?.displayName ?: "" },
                        avatarUrl = finalAvatarUrl
                    )

                    userRepository.upsertUser(updatedUser)
                }

                _updateStatus.value = Resource.success("Cập nhật thành công!")
            } catch (e: Exception) {
                _updateStatus.value = Resource.error(e.message ?: "Có lỗi xảy ra", null)
            }
        }
    }

    // Hàm để reset status sau khi thông báo xong
    fun resetUpdateStatus() {
        _updateStatus.value = null
    }
}