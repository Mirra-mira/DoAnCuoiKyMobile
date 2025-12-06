<<<<<<<< HEAD:app/src/main/java/com/example/doancuoikymobile/ui/MainActivity.kt
package com.example.doancuoikymobile.ui
========
package com.example.doancuoikymobile.ui.activity
>>>>>>>> ff4d89ca65791e9d95219902c6a6e481cf3e46b0:app/src/main/java/com/example/doancuoikymobile/ui/activity/MainActivity.kt

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
<<<<<<<< HEAD:app/src/main/java/com/example/doancuoikymobile/ui/MainActivity.kt
========
import androidx.compose.material3.Card
>>>>>>>> ff4d89ca65791e9d95219902c6a6e481cf3e46b0:app/src/main/java/com/example/doancuoikymobile/ui/activity/MainActivity.kt
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
<<<<<<<< HEAD:app/src/main/java/com/example/doancuoikymobile/ui/MainActivity.kt
import com.example.doancuoikymobile.ui.theme.DoAnCuoiKyMobileTheme
import androidx.compose.material3.Card
import androidx.compose.ui.Alignment
import androidx.fragment.app.Fragment
import com.example.doancuoikymobile.R
import com.example.doancuoikymobile.databinding.ActivityMainBinding
import com.example.doancuoikymobile.ui.fragment.HomeFragment
import com.example.doancuoikymobile.ui.fragment.LibraryFragment
import com.example.doancuoikymobile.ui.fragment.SearchFragment

========
import androidx.fragment.app.Fragment
import com.example.doancuoikymobile.ui.home.HomeFragment
import com.example.doancuoikymobile.ui.playlist.LibraryFragment
import com.example.doancuoikymobile.R
import com.example.doancuoikymobile.ui.search.SearchFragment
import com.example.doancuoikymobile.databinding.ActivityMainBinding
import com.example.doancuoikymobile.ui.profile.ProfileFragment
import com.example.doancuoikymobile.ui.theme.DoAnCuoiKyMobileTheme
>>>>>>>> ff4d89ca65791e9d95219902c6a6e481cf3e46b0:app/src/main/java/com/example/doancuoikymobile/ui/activity/MainActivity.kt

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
                R.id.bottom_nav_profile -> {
                    replaceFragment(ProfileFragment())
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

        Scaffold(modifier = Modifier.Companion.fillMaxSize()) { innerPadding ->
            Column(
                modifier = Modifier.Companion
                    .fillMaxSize()

                    .padding(innerPadding),
                horizontalAlignment = Alignment.Companion.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                FilledCardExample()
            }
        }
    }


    @Composable
    fun FilledCardExample() {
        HorizontalDivider(thickness = 2.dp)
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.background,
            ),
            modifier = Modifier.Companion.fillMaxWidth().height(100.dp)
        ) {
            Text(
                text = "Card phat nhac",
                modifier = Modifier.Companion
                    .fillMaxSize()
                    .padding(16.dp),
                textAlign = TextAlign.Companion.Center,
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