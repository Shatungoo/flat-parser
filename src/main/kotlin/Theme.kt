import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
@Preview
fun Settings(
    onCloseRequest: () -> Unit,
    settings: Settings
) {
    val darkTheme = remember { mutableStateOf(settings.darkTheme) }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = Modifier.height(30.dp)) {
            Button(onClick = onCloseRequest) {
                Text("Close")
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text("Settings")
        }
        Spacer(modifier = Modifier.height(1.dp))
        SettingBoolean("Dark theme", darkTheme)
        Setting("Editor") {
            optionsMenu()
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun optionsMenu() {
    val options = listOf("Option 1", "Option 2", "Option 3", "Option 4", "Option 5")
    var expanded by remember { mutableStateOf(false) }
    var selectedOptionText by remember { mutableStateOf(options[0]) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {

        TextField(
            readOnly = true,
            value = selectedOptionText,
            onValueChange = { },
            singleLine = true,
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { selectionOption ->
                DropdownMenuItem(
                    onClick = {
                        selectedOptionText = selectionOption
                        expanded = false
                    }
                ) {
                    Text(text = selectionOption)
                }
            }
        }
    }
}

@Composable
fun SettingBoolean(
    text: String,
    checked: MutableState<Boolean>
) {
    Setting(text = text) {
        Switch(checked = checked.value,
            onCheckedChange = { checked.value = it })
    }
}

@Composable
fun Setting(text: String, function: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .width(400.dp)
            .height(50.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            modifier = Modifier.wrapContentHeight(),
            textAlign = TextAlign.Left,
        )
        Spacer(modifier = Modifier.weight(1f))
        function()
    }
}


