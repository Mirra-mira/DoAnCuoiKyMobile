package com.example.doancuoikymobile.ui.theme




import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.doancuoikymobile.R


val MomoTrustSans @Composable get() = FontFamily(
    Font(R.font.momotrustsans_extralight, FontWeight.Light),
    Font(R.font.momotrustsans_light, FontWeight.ExtraLight),
    Font(R.font.momotrustsans_regular, FontWeight.Normal),
    Font(R.font.momotrustsans_medium, FontWeight.Medium),
    Font(R.font.momotrustsans_semibold, FontWeight.SemiBold),
    Font(R.font.momotrustsans_bold, FontWeight.Bold),
    Font(R.font.momotrustsans_extrabold, FontWeight.ExtraBold),
)

val Typography : Typography @Composable get() = Typography(
    headlineLarge = TextStyle(
        fontFamily = MomoTrustSans,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),

    bodyLarge = TextStyle(
        fontFamily = MomoTrustSans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),

    labelLarge = TextStyle(
        fontFamily = MomoTrustSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )

)