package com.example.doancuoikymobile.ui.activity // Cập nhật đúng package mới

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.example.doancuoikymobile.R
import com.example.doancuoikymobile.databinding.ActivityMainBinding
import com.example.doancuoikymobile.ui.theme.DoAnCuoiKyMobileTheme

// QUAN TRỌNG: Import các Fragment từ đúng vị trí mới (Refactor)
import com.example.doancuoikymobile.ui.home.HomeFragment
import com.example.doancuoikymobile.ui.playlist.LibraryFragment
import com.example.doancuoikymobile.ui.search.SearchFragment
import com.example.doancuoikymobile.ui.profile.ProfileFragment

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private lateinit var miniPlayerBar: CardView
    private lateinit var tvMiniTitle: TextView
    private lateinit var tvMiniSubtitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Mặc định vào HomeFragment
        replaceFragment(HomeFragment())

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_nav_home -> {
                    replaceFragment(HomeFragment())
                    true
                }

                R.id.bottom_nav_search -> {
                    replaceFragment(SearchFragment())
                    true
                }

                R.id.bottom_nav_lib -> {
                    replaceFragment(LibraryFragment())
                    true
                }

                R.id.bottom_nav_profile -> {
                    replaceFragment(ProfileFragment())
                    true
                }

                else -> false
            }

//        val composeView = binding.composeView
//        composeView.setContent {
//            DoAnCuoiKyMobileTheme(dynamicColor = false) {
//                //MainContent()
//            }
//        }
        }
        miniPlayerBar = findViewById(R.id.cardMiniPlayer)
        tvMiniTitle = miniPlayerBar.findViewById(R.id.tvMiniTitle)
        tvMiniSubtitle = miniPlayerBar.findViewById(R.id.tvMiniSubtitle)
        miniPlayerBar.setOnClickListener{}
        val composeView = binding.composeView
        composeView.setContent {
            DoAnCuoiKyMobileTheme(dynamicColor = false) {
                //MainContent()
            }
        }
    }
    fun showMiniPlayer(title: String, subtitle: String) {
        miniPlayerBar.visibility = View.VISIBLE
        tvMiniTitle.text = title
        tvMiniSubtitle.text = subtitle
        // TODO: Cập nhật icon Play/Pause và thanh tiến trình
    }
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, fragment)
            .addToBackStack(null)
            .commit()
    }
}

//    @Composable
//    fun MainContent() {
//        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//            Column(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(innerPadding),
//                horizontalAlignment = Alignment.CenterHorizontally,
//                verticalArrangement = Arrangement.Center
//            ) {
//                FilledCardExample()
//            }
//        }
//    }
//
//    @Composable
//    fun FilledCardExample() {
//        HorizontalDivider(thickness = 2.dp)
//        Card(
//            colors = CardDefaults.cardColors(
//                containerColor = MaterialTheme.colorScheme.background,
//            ),
//            modifier = Modifier.fillMaxWidth().height(100.dp)
//        ) {
//            Text(
//                text = "Card phat nhac",
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(16.dp),
//                textAlign = TextAlign.Center,
//            )
//        }
//    }
//
//    @Preview(showBackground = true)
//    @Composable
//    fun MainContentPreview() {
//        DoAnCuoiKyMobileTheme {
//            MainContent()
//        }
//    }
//}