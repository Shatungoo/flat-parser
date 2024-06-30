import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowScope


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

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun optionsMenu(options: List<String>,
                selectedOption: MutableState<String>
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.width(200.dp)
    ) {

        TextField(
            readOnly = true,
            value = selectedOption.value,
            onValueChange = { },
            singleLine = true,
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach {
                DropdownMenuItem(
                    onClick = {
                        selectedOption.value = it
                        expanded = false
                    }
                ) {
                    Text(text = it)
                }
            }
        }
    }
}