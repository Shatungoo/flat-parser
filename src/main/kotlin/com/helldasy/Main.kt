package com.helldasy

import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.helldaisy.ui.Theme
import com.helldasy.ui.*

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
                Views.Map -> MapView(settings,
                    back = { settings.view.value = Views.Main },
                    selectFlat = ::selectFlat
                    )
                Views.Flat -> FlatCardView(settings.selectedFlat.value!!, back = { settings.view.value = Views.Main })
                Views.ImageGallery -> ImageGallery(settings.selectedImage.value!!)
//                    back = { settings.view.value = Views.Main
            }
        }
    }
}

fun selectFlat(flat: Response.Flat) {
    settings.selectedFlat.value = flat
    settings.view.value = Views.Flat
}

fun selectImage(image: SelectedImage) {
    settings.selectedImage.value = image
    settings.view.value = Views.Flat
}

enum class Views {
    Main, Settings, Map, Flat, ImageGallery
}
