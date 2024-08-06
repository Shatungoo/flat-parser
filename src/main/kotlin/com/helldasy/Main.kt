package com.helldasy

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.helldaisy.ui.Theme
import com.helldasy.ui.*

val settings = loadSettings()
val state: MutableState<State> = mutableStateOf(FlatsState(
    flats = settings.flats.value,
))


fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = appName,
        state = rememberWindowState(placement = WindowPlacement.Maximized),
        icon = painterResource("app.png"),
    ) {
        Theme {
            val scrollState = rememberLazyListState()
            when (val currentState = state.value){
                is FlatsState -> {
                        Column {
                            ControlPanel(state, settings, )
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(modifier = Modifier.fillMaxWidth().weight(1f).align(Alignment.CenterHorizontally)) {
                                LazyColumn(userScrollEnabled = true, state = scrollState) {
                                    items(currentState.flats) { flat ->
                                        FlatCard(flat = flat,
                                            selectImage = { urls, id, selected -> state.value = ImageState(urls, id, selected, currentState) },
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                    }
                                }
                            }
                        }
                    MainView(settings = settings, state)
                }
                is FlatState -> {
                    FlatCardView(flat = currentState.flat,
                        back = { state.value = currentState.previous!! },
                        selectImage = { urls, id, selected -> state.value = ImageState(urls, id, selected, currentState) },
                    )
                }
                is ImageState -> {
                    BigImageGallery(
                        urls = currentState.urls,
                        id = currentState.id,
                        selectedImage = mutableStateOf(currentState.selectedImage),
                        onClick = { state.value = currentState.previous }
                    )
                }
                is MapState -> {
                    MapView(currentState.flats,
                        back = { state.value = currentState.previous!! },
                        selectFlat = { state.value = FlatState(it, currentState) }
                    )
                }
            }

        }
    }
}

interface State {
    val previous: State?
}
data class FlatsState(
    val flats: List<Response.Flat> = emptyList(),
    override val previous: State? = null,
) : State

data class FlatState(
    val flat: Response.Flat,
    override val previous: State? = null,
) : State

data class ImageState(
    val urls: List<String> = emptyList(),
    val id: String,
    val selectedImage: Int = 0,
    override val previous: State,
) : State

data class MapState(
    val flats: List<Response.Flat>,
    override val previous: State? = null,
) : State
