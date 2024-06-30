import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.darkColors
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application


fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = appName,
        icon = painterResource("app.png"),
    ) {
        val settings = loadSettings()
        MaterialTheme(
            colors = darkColors()
        ) {
            Surface(Modifier.fillMaxSize()) {
                val view = remember { mutableStateOf(Views.Main) }
                when (view.value) {
                    Views.Settings -> Settings(
                        onCloseRequest = { view.value = Views.Main },
                        settings = settings
                    )

                    Views.Main -> MainView(view = view)
                }
            }
        }
    }
}

enum class Views {
    Main, Settings
}
