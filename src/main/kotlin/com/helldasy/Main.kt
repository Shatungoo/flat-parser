package com.helldasy

import com.helldasy.ui.MainView
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.helldaisy.ui.Theme
import com.helldasy.ui.Settings


fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = appName,
        state = rememberWindowState(placement = WindowPlacement.Maximized),
        icon = painterResource("app.png"),
    ) {
        val settings = loadSettings()
        Theme {
            val view = remember { mutableStateOf(Views.Main) }
            when (view.value) {
                Views.Settings -> Settings(
                    onCloseRequest = {
                        settings.saveSettings()
                        view.value = Views.Main
                    },
                    settings = settings
                )

                Views.Main -> MainView(view = view)
            }
        }
    }
}

enum class Views {
    Main, Settings//ImageGallery
}
