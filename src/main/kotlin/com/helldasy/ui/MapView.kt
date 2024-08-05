package com.helldasy.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.window.singleWindowApplication
import com.helldasy.map.MapSwing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.helldasy.*
import com.helldasy.map.ClickablePoint
import com.helldasy.map.MapComposeBig
import org.jxmapviewer.viewer.GeoPosition
import java.nio.file.Paths

fun main() = singleWindowApplication {

    val path = Paths.get(settingsPath, "flats").toAbsolutePath().toString()
    val db = Db(path)
    val flats = db.getFlats()
    val selectedFlats = mutableStateOf(emptyList<Response.Flat>())

    Box {
        MapSwing(
            points = flats,
            onClick = {
                selectedFlats.value = it
            },
        )
        Button(
            onClick = {
                selectedFlats.value = emptyList()
            },
//            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
        }
    }
}


@Composable
fun MapView(
    settings: Settings,
    back: () -> Unit = {},
    selectFlat: (Response.Flat) -> Unit = {},
) {
    val flats = settings.flats
    val selectedFlats = mutableStateOf(emptyList<Response.Flat>())
    Row {
        Box {
            LazyColumn(modifier = Modifier.width(400.dp)) {
                item {
                    BackButtonAct { back() }
                }
                selectedFlats.value.map { flat ->
                    item {
                        SmallFlatCard(flat, onClick = { selectFlat(flat) })
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }

        }

        Row {
//            MapSwing(
//                points = flats.value,
//                onClick = {
//                    selectedFlats.value = it
//                },
//            )

            MapComposeBig(
                points = flats.value.mapNotNull {
                    if (it.lat != null && it.lng != null)
                        ClickablePoint(GeoPosition(it.lat, it.lng), it) else null
                },
                zoom = 5,
                onClick = {
                    selectedFlats.value = it.map { it.data }
                },
            )
        }
    }
}
