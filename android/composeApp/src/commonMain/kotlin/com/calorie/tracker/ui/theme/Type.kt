package com.calorie.tracker.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun getFlip7Typography(): Typography {
    return Typography(
        // headline-lg / displayLarge
        displayLarge = TextStyle(
            fontFamily = FontFamily.Default, // uses system/default (Inter)
            fontWeight = FontWeight.Bold, // 700
            fontSize = 32.sp,
            lineHeight = 40.sp,
            letterSpacing = (-0.64).sp // -0.02em
        ),
        // headline-lg-mobile / displayMedium
        displayMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold, // 700
            fontSize = 28.sp,
            lineHeight = 36.sp,
            letterSpacing = (-0.56).sp // -0.02em
        ),
        // headline-md / headlineLarge
        headlineLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold, // 600
            fontSize = 24.sp,
            lineHeight = 32.sp,
            letterSpacing = (-0.24).sp // -0.01em
        ),
        // body-lg / bodyLarge
        bodyLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal, // 400
            fontSize = 18.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.18.sp // 0.01em
        ),
        // body-md / bodyMedium
        bodyMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal, // 400
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.16.sp // 0.01em
        ),
        // label-md / labelLarge
        labelLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold, // 600
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.7.sp // 0.05em
        ),
        // label-sm / labelMedium
        labelMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium, // 500
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.24.sp // 0.02em
        )
    )
}
