package com.helldasy.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.helldasy.*

val filterView = mutableStateOf(false)
val filterParserView = mutableStateOf(false)

@Composable
fun MainView(settings: Settings, state: MutableState<State>) {
    if (filterView.value) FilterView(state, settings)
    else if (filterParserView.value) FilterParser(settings)
}

@Composable
fun FilterView(
    state: MutableState<State>,
    settings: Settings,
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Row {
            Box(
                modifier = Modifier.fillMaxHeight().background(Color.Black).width(400.dp)
            ) {
                FilterDb(settings.filterDb) {
                    state.value = FlatsState(flats = settings.db.getFlats(settings.filterDb.value))
                }
            }
            Box(
                modifier = Modifier.fillMaxSize().clickable(onClick = {
                    filterView.value = false
                })
            )
        }
    }
}

@Composable
fun FilterParser(settings: Settings) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Row {
            Box(
                modifier = Modifier.fillMaxHeight().background(Color.Black).width(400.dp)
            ) {
                FilterParser(
                    settings.baseUrl,
                    settings.urlParamMap,
                ) {}
            }
            Box(
                modifier = Modifier.fillMaxSize().clickable(onClick = {
                    filterParserView.value = false
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
    val flats = settings.flats.value
    val filterDb = settings.filterDb.value
    Card {
        Row(
            modifier = Modifier.height(40.dp), horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            val btnName = mutableStateOf("Update DB")
            BtnWithSettings(name = btnName, action = {
                btnName.value = "In progress..."
                updateDb(db) {
                    val flats = db.getFlats(filterDb)
                    btnName.value = "Update DB"

                    state.value = current.copy(flats = flats)
                }
            }, settings = {
                filterParserView.value = true
            })
            controlPanelButton(onClick = {
                filterView.value = true
            }, text = "Filter")
            controlPanelButton(onClick = {
                state.value = MapState(flats = flats, previous = current)
            }, text = "Show on map")

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
        modifier = Modifier.fillMaxHeight(), onClick = onClick
    ) {
        if (image != null) Image(painterResource(image), "image")
        if (text != null) Text(text)
    }
}

