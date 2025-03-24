package com.helldaisy.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.helldaisy.*
import com.helldaisy.State
import kotlinx.coroutines.*

enum class FilterState {
    none, combined
}
val filterView = mutableStateOf(FilterState.none)

@Composable
fun MainView(settings: Settings, state: MutableState<State>) {
    when (filterView.value) {
        FilterState.combined -> FilterCombined(settings.filterCombined,
            onSearch = {
                settings.saveSettings()
                updateDb(settings.db, settings.filterCombined) {
                    val flatsUpdate = settings.db.getFlats(settings.filterCombined)
                    state.value = (state.value as FlatsState).copy(flats = flatsUpdate)
                    filterView.value = FilterState.none
                }
            },
            onClose = { filterView.value = FilterState.none })
        else -> {}
    }
}

@Composable
private fun FilterCombined(
    filter: Filter,
    onSearch: () -> Unit,
    onClose: () -> Unit,
) {

    Box(
        modifier = Modifier.fillMaxSize()

    ) {
        Row {
            Box(
                modifier = Modifier.fillMaxHeight().background(Color.Black).width(400.dp)
            ) {
                FilterCombined(
                    filter,
                    apply = {
                        onSearch()
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
    Card(modifier = Modifier.fillMaxWidth().height(45.dp).padding(3.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically

        ) {

            //Get flats from site
            BtnWithSettings(
                name = mutableStateOf("Search"),
                action = {
//                    btnName.value = "In progress..."
                    updateDb(db, settings.filterCombined) {
                        val flatsUpdate = db.getFlats(settings.filterCombined)
//                        btnName.value = "Update DB"

                        state.value = current.copy(flats = flatsUpdate)
//                        filterView.value = FilterState.none
                    }

                },
                settings = { filterView.value = FilterState.combined }
            )
            controlPanelButton(onClick = {
                state.value = MapState(
                    map = MapViewState(flats),
                    previous = current
                )
            }, text = "Show on map")
            Spacer(modifier = Modifier.weight(1f))
            UpdateButton()
            Text(
                "Flats: ${flats.size}", modifier = Modifier.padding(5.dp),
                color = MaterialTheme.colors.onPrimary
            )
        }
    }
}



val updateState = mutableStateOf(UpdateState.Idle)

@Composable
fun UpdateButton() {
    LaunchedEffect(Unit) {
        updateState.value = checkUpdate()
    }

    when (updateState.value) {
        UpdateState.Idle, UpdateState.NotAvailable -> {}
        UpdateState.Available -> {
            controlPanelButton(onClick = {
                CoroutineScope(Dispatchers.Default).launch {
                    downloadLatest {
                        updateState.value = UpdateState.Downloaded
                    }
                }
                updateState.value = UpdateState.Downloading
            }, text = "Download update")
        }
        UpdateState.Downloading -> {
            OutlinedButton(onClick = {}) {
                Text("Downloading...")
            }
        }
        UpdateState.Downloaded -> {
            controlPanelButton(onClick = {
                updateApp()
            }, text = "Update")
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

