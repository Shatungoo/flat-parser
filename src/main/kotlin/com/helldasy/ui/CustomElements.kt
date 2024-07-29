package com.helldasy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowScope
import java.awt.Desktop
import java.net.URI


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
fun optionsMenu(
    options: List<String>,
    selectedOption: MutableState<String>,
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

@Composable
fun BtnWithSettings(
    name: MutableState<String>,
    action: () -> Unit,
    settings: () -> Unit,
) {
    Box(
        modifier = Modifier.clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colors.primary)
    ) {
        Row {

            Button(
                modifier = Modifier.fillMaxHeight(),
                onClick = action,
            ) {
                Text(name.value)
            }
            Button(
                modifier = Modifier.fillMaxHeight().width(50.dp),
                onClick = settings
            ) {
                Icon(
                    modifier = Modifier.size(50.dp),
                    imageVector = Icons.Filled.Settings,
                    contentDescription = null
                )
            }
        }
    }
}

fun openInBrowser(url: String) {
    Desktop.getDesktop().browse(URI.create(url))
}
