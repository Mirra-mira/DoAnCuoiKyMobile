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
import com.example.doancuoikymobile.ui.auth.AuthActivity // Import AuthActivity mới
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            val splashScreen = installSplashScreen()
            splashScreen.setKeepOnScreenCondition { true }
        }
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                delay(3000)

                // Giả định kiểm tra trạng thái đăng nhập (Bạn có thể thay đổi logic này sau)
                val isLoggedIn = false // Mặc định là chưa đăng nhập

                val targetActivity = if (isLoggedIn) {
                    MainActivity::class.java
                } else {
                    AuthActivity::class.java // Chuyển sang màn hình Đăng nhập/Đăng ký
                }

                val intent = Intent(this@SplashActivity, targetActivity)
                startActivity(intent)
                finish()
            }
        }
    }
}