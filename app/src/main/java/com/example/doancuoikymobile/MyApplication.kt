package com.example.doancuoikymobile

import android.app.Application
import com.google.firebase.FirebaseApp
import com.example.doancuoikymobile.debug.DebugRunner
import com.example.doancuoikymobile.repository.UserRepository
import com.example.doancuoikymobile.data.remote.firebase.UserRemoteDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // 1. Khởi tạo Firebase
        FirebaseApp.initializeApp(this)

        // 2. Khởi tạo hệ thống (Tạo tài khoản Admin nếu chưa có)
        initializeSystemData()

        // 3. Chạy các bài test Debug (Chỉ nên bật khi cần test)
        // Lưu ý: DebugRunner nên được gọi sau khi Firebase đã sẵn sàng
        DebugRunner.runAll()
    }

    private fun initializeSystemData() {
        val userRepo = UserRepository(UserRemoteDataSource())

        // Sử dụng CoroutineScope để chạy tác vụ treo (suspend) trong Application class
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Gọi hàm khởi tạo Admin đã viết trong UserRepository
                userRepo.initializeAppSystem()
            } catch (e: Exception) {
                // Log lỗi nếu khởi tạo thất bại
                android.util.Log.e("MyApplication", "Failed to init system: ${e.message}")
            }
        }
    }
}