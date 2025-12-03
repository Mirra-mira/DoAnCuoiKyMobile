package com.example.doancuoikymobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.doancuoikymobile.ui.theme.DoAnCuoiKyMobileTheme
import com.example.doancuoikymobile.ui.theme.MomoTrustSans
import com.example.doancuoikymobile.ui.theme.Typography
import androidx.compose.material3.Card
import androidx.compose.ui.Alignment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.doancuoikymobile.databinding.ActivityMainBinding




class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
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
                else -> false
            }
        }

        val composeView = binding.composeView
        composeView.setContent {
            DoAnCuoiKyMobileTheme(dynamicColor = false) {
                MainContent()
            }
        }

    }

    private fun replaceFragment(fragment : Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, fragment)
            .addToBackStack(null)
            .commit()

    }

    @Composable
    fun MainContent() {

        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()

                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                FilledCardExample()
            }
        }
    }


    @Composable
    fun FilledCardExample() {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.background,
            ),
            modifier = Modifier.fillMaxWidth().height(100.dp)
        ) {
            Text(
                text = "Card phat nhac",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                textAlign = TextAlign.Center,
            )
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun MainContentPreview() {
        DoAnCuoiKyMobileTheme {
            MainContent()
        }
    }
}

