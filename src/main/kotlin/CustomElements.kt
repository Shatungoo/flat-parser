import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.singleWindowApplication


fun main() = singleWindowApplication(
    title = appName,
    icon = painterResource("app.png"),
) {
    val settings = loadSettings()
    Theme {
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


enum class Views {
    Main, Settings
}
