import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application


fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = appName,
        icon = painterResource("app.png"),
    ) {
        val settings = loadSettings()
        MaterialTheme {
            var isDialogOpen by remember { mutableStateOf(false) }
            Row(modifier = Modifier.height(50.dp)) {
                Button(onClick = { isDialogOpen = true }) {
                    Image(painterResource("settings.png"), "image")
                }
            }

            if (isDialogOpen) {
                Settings(
                    onCloseRequest = { isDialogOpen = false },
                    settings = settings
                )
            }
        }
    }
}