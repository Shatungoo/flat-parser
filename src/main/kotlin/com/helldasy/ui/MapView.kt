package com.helldasy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.window.singleWindowApplication
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.helldasy.*
import com.helldasy.map.*
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
    Box {

        MapComposeBig(
            points = flats.value.mapNotNull {
                if (it.lat != null && it.lng != null)
                    SelectablePoint(geoPoistion = GeoPosition(it.lat, it.lng), data = it) else null
            },
            zoom = 5,
            onClick = {
                selectedFlats.value = it.toList()
            },
        )
        Column {
            BackButtonAct { back() }
            LazyColumn(modifier = Modifier.width(400.dp).padding(5.dp).background(MaterialTheme.colors.surface)) {
                item {

                }
                selectedFlats.value.map { flat ->
                    item {
                        SmallFlatCard(flat, onClick = { selectFlat(flat) })
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                }
            }
        }

    }
}

