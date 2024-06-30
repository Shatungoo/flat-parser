import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.ui.window.WindowScope
import androidx.compose.ui.window.application


@Composable
fun WindowScope.AppWindowTitleBar() = WindowDraggableArea {
    Box(Modifier.fillMaxWidth().height(35.dp).background(Color.Black))
    Row(modifier = Modifier.fillMaxHeight()) {
        Spacer(Modifier.weight(1f))
        Button(onClick = { println("Minimize") }) {
            Text("_")
//            Icon(Icons.Default.Add, contentDescription = "Minimize")
        }
        Button(onClick = { println("Minimize") }) {
            Text("▭")
//            Icon(Icons.Default.Add, contentDescription = "Minimize")
        }
        Button(onClick = { println("Minimize") }) {
            Text("X")
//            Icon(Icons.Default.Add, contentDescription = "Minimize")
        }
    }
}