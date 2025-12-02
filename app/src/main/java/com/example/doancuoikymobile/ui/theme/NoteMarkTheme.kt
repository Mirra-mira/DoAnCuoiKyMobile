package com.example.doancuoikymobile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable


val lightColorTheme = lightColorScheme(

)

val darkColorTheme = darkColorScheme()

@Composable
fun NoteMarkTheme(
    content: @Composable () -> Unit

) {
    val theme = if (isSystemInDarkTheme()) darkColorTheme else lightColorTheme
    MaterialTheme(
        colorScheme = theme,
        content = content
    )
}

