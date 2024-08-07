package com.helldaisy.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.helldaisy.Settings

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


