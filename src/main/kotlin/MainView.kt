import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun MainView(view: MutableState<Views>) {
    Row {
        ControlPanel(view)
    }
}

@Composable
fun ControlPanel(view: MutableState<Views>) {
    Row(modifier = Modifier.height(50.dp)) {
        controlPanelButton(onClick = { view.value = Views.Settings }, "settings.png")
        controlPanelButton(onClick = { view.value = Views.Settings }, "settings.png")
    }
}

@Composable
fun controlPanelButton(
    onClick: () -> Unit,
    image: String
) {
    Button(
        onClick = onClick,
    ) {
        Image(painterResource(image), "image")
    }
}