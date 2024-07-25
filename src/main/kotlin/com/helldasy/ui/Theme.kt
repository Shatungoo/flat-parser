package com.helldaisy.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.Surface
import androidx.compose.material.darkColors
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

@Composable
fun Theme(function: @Composable () -> Unit) {
    MaterialTheme(
        colors = darkColors().copy(
            primary = Color(0xFF0366d6),
            primaryVariant = Color(0xFF0331f4),
            secondary = Color(0xFF2f363d),
            secondaryVariant = Color(0xFF9fadaf),
            background = Color(0xFF121212),
            surface = Color(0xFF1e1f22),
            error = Color(0xFFf44f03),
            onPrimary = Color.LightGray,
            onSecondary = Color.LightGray,
            onBackground = Color.DarkGray,
            onSurface = Color.White,
            onError = Color.Red
        ),
        typography = typography.copy(
            h1 = typography.h1.copy(fontSize = 20.sp, color = Color.White),
            h2 = typography.h2.copy(fontSize = 18.sp, color = Color.White),
            h3 = typography.h3.copy(fontSize = 16.sp, color = Color.White),
            h4 = typography.h4.copy(fontSize = 14.sp, color = Color.White),
            h5 = typography.h5.copy(fontSize = 12.sp, color = Color.White),
            h6 = typography.h6.copy(fontSize = 10.sp, color = Color.White),
            subtitle1 = typography.subtitle1.copy(fontSize = 16.sp, color = Color.White),
            subtitle2 = typography.subtitle2.copy(fontSize = 14.sp, color = Color.White),
            body1 = typography.body1.copy(fontSize = 12.sp, color = Color.White),
            body2 = typography.body2.copy(fontSize = 10.sp, color = Color.LightGray),
            button = typography.button.copy(fontSize = 12.sp, color = Color.White),
            caption = typography.caption.copy(fontSize = 12.sp, color = Color.White),
            overline = typography.overline.copy(fontSize = 10.sp, color = Color.White),
        )
    ) {
        Surface(Modifier.fillMaxSize()) {
            function()
        }
    }
}
