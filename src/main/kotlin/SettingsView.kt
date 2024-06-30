import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
@Preview
fun Settings(
    onCloseRequest: () -> Unit,
    settings: Settings
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = Modifier.height(30.dp)) {
            Button(onClick = onCloseRequest) {
                Text("Close")
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text("Settings")
        }
        Spacer(modifier = Modifier.height(1.dp))
        SettingBoolean("Dark theme", settings.darkTheme)
        Setting("Theme") {
            optionsMenu(listOf("Dark", "Light", "System"),
                settings.theme)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun optionsMenu(options: List<String>,
                selectedOption: MutableState<String>) {
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


