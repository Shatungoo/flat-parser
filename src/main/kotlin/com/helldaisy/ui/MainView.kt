package com.helldaisy.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.helldaisy.*
import com.helldaisy.State

val filterDbView = mutableStateOf(false)
val filterParserView = mutableStateOf(false)

@Composable
fun MainView(settings: Settings, state: MutableState<State>) {

    if (filterDbView.value) FilterDb(state, settings, close = { filterDbView.value = false })
    else if (filterParserView.value) FilterParser1(settings.filterParser, onClose = { filterParserView.value = false })

}

@Composable
fun FilterDb(
    state: MutableState<State>,
    settings: Settings,
    close: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Row {
            Box(
                modifier = Modifier.fillMaxHeight().background(Color.Black).width(400.dp)
            ) {
                FilterDb(settings.filterDb) {
                    settings.saveSettings()
                    state.value = FlatsState(flats = settings.db.getFlats(settings.filterDb))
                    close()
                }
            }
            Box(
                modifier = Modifier.fillMaxSize().clickable(onClick = {
                    close()
                })
            )
        }
    }
}

@Composable
private fun FilterParser1(
    filter: Filter,
    onClose: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Row {
            Box(
                modifier = Modifier.fillMaxHeight().background(Color.Black).width(400.dp)
            ) {
                FilterParser(filter,
                    onClick = {
                        settings.saveSettings()
                        onClose()
                    }
                )
            }
            Box(
                modifier = Modifier.fillMaxSize().clickable(onClick = {
                    onClose()
                })
            )
        }
    }
}


@Composable
fun ControlPanel(
    state: MutableState<State>,
    settings: Settings,
) {
    val db = settings.db
    val current = state.value as FlatsState
    val flats = current.flats
    val filterDb = settings.filterDb
    Card(modifier = Modifier.fillMaxWidth().height(45.dp).padding(3.dp)) {
        Row(
//            modifier = Modifier.height(40.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically

        ) {
            val btnName = mutableStateOf("Update DB")
            //Get flats from site
            BtnWithSettings(name = btnName, action = {
                btnName.value = "In progress..."
                updateDb(db) {
                    val flatsUpdate = db.getFlats(filterDb)
                    btnName.value = "Update DB"

                    state.value = current.copy(flats = flatsUpdate)
                    filterParserView.value = false
                }
            }, settings = { filterParserView.value = true })
            // Get flats from db
            BtnWithSettings(name = mutableStateOf("Search"),
                action = {
                    val flatsUpdate = db.getFlats(filterDb)
                    state.value = current.copy(flats = flatsUpdate)
                    filterDbView.value = false

                },
                settings = { filterDbView.value = true }
            )

            controlPanelButton(onClick = {
                state.value = MapState(flats = flats, previous = current)
            }, text = "Show on map")
            Spacer(modifier = Modifier.weight(1f))
            Text("Flats: ${flats.size}", modifier = Modifier.padding(5.dp),
//                fontStyle = MaterialTheme.typography.subtitle1.fontStyle,
                color = MaterialTheme.colors.onPrimary)


        }
    }
}

@Composable
fun controlPanelButton(
    onClick: () -> Unit,
    image: String? = null,
    text: String? = null,
) {
    Button(
        modifier = Modifier, onClick = onClick
    ) {
        if (image != null) Image(painterResource(image), "image")
        if (text != null) Text(text)
    }
}

