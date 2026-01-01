package com.example.doancuoikymobile.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.doancuoikymobile.repository.AuthRepository
import com.example.doancuoikymobile.ui.auth.AuthActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity: ComponentActivity() {
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            val splashScreen = installSplashScreen()
            splashScreen.setKeepOnScreenCondition { true }
        }
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                delay(2000) // Delay ngắn để hiển thị splash screen

                // Kiểm tra trạng thái đăng nhập từ Firebase/AuthRepository
                val currentUser = authRepository.getCurrentUser()
                val isLoggedIn = currentUser != null

                val targetActivity = if (isLoggedIn) {
                    MainActivity::class.java
                } else {
                    AuthActivity::class.java
                }

                // Tạo Intent với flags để ngăn back navigation
                val intent = Intent(this@SplashActivity, targetActivity).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                
                startActivity(intent)
                finish() // Đóng SplashActivity để không thể back về
            }
        }
    }
}