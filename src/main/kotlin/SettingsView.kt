import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState

@Composable
@Preview
fun Settings(
    onCloseRequest: (result: String?) -> Unit,
    settings: Settings
) {
    DialogWindow(
        onCloseRequest = {
            settings.saveSettings()
            onCloseRequest(null)
        },
        state = rememberDialogState(position = WindowPosition(Alignment.Center))
    ) {
        val darkTheme = remember { mutableStateOf(settings.darkTheme) }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(modifier = Modifier.height(30.dp)) {
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
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun optionsMenu() {

    val options = listOf("Option 1", "Option 2", "Option 3", "Option 4", "Option 5")
    var expanded by remember { mutableStateOf(false) }
    var selectedOptionText by remember { mutableStateOf(options[0]) }

    ExposedDropdownMenuBox(
        modifier = Modifier.fillMaxHeight(0.9f),
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
        }
    ) {
        TextField(
            readOnly = true,
            value = selectedOptionText,
            onValueChange = { },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            },
            modifier = Modifier.fillMaxHeight(),
            textStyle = TextStyle.Default.copy(fontSize = 14.sp),
            colors = ExposedDropdownMenuDefaults.textFieldColors()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
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
            .height(50.dp)
            .background(color = MaterialTheme.colors.primary),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            modifier = Modifier.wrapContentHeight(),
            textAlign = TextAlign.Left,
            color = MaterialTheme.colors.onPrimary
        )
        Spacer(modifier = Modifier.weight(1f))
        function()
    }
}


