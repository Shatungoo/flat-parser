package com.helldasy

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.helldaisy.ui.Theme
import com.helldasy.ui.MainView
import com.helldasy.ui.Settings

val settings = loadSettings()

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = appName,
        state = rememberWindowState(placement = WindowPlacement.Maximized),
        icon = painterResource("app.png"),
    ) {
        Theme {
            when (settings.view.value) {
                Views.Settings -> Settings(
                    onCloseRequest = {
                        settings.saveSettings()
                        settings.view.value = Views.Main
                    },
                    settings = settings
                )

                Views.Main -> MainView(settings = settings)
            }
        }
    }
}

enum class Views {
    Main, Settings
}
