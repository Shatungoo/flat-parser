package com.helldasy.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.window.singleWindowApplication
import com.helldasy.map.Map
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.helldasy.*
import java.nio.file.Paths

fun main() = singleWindowApplication {

    val path = Paths.get(settingsPath, "flats").toAbsolutePath().toString()
    val db = Db(path)
    val flats = db.getFlats()
    val selectedFlats = mutableStateOf(emptyList<Response.Flat>())

    Box {
        Map(
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
//        Spacer(modifier = Modifier.weight(1f))

        Box {
            LazyColumn(modifier = Modifier.width(400.dp)) {
                item{
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
            Map(
                points = flats.value,
                onClick = {
                    selectedFlats.value = it
                },
            )
        }
    }
}
