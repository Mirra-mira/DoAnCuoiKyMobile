package com.example.doancuoikymobile.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.doancuoikymobile.R
import com.example.doancuoikymobile.ui.activity.MainActivity

class AuthActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        // Mặc định hiển thị màn hình Đăng Nhập
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.authContainer, LoginFragment())
                .commit()
        }
    }

    // Hàm chuyển sang màn hình chính (MainActivity)
    fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Đóng AuthActivity để user không back lại được màn login
    }
}