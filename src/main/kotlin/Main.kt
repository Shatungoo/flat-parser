import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.singleWindowApplication


fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = appName,
//        undecorated = true,
        icon = painterResource("app.png"),
    ) {
        val settings = loadSettings()
//        Column {
//            AppWindowTitleBar()
            Theme {
                val view = remember { mutableStateOf(Views.Main) }
                when (view.value) {
                    Views.Settings -> Settings(
                        onCloseRequest = { view.value = Views.Main },
                        settings = settings
                    )

                    Views.Main -> MainView(view = view)
                }
//            }
        }


    }
}

enum class Views {
    Main, Settings
}
