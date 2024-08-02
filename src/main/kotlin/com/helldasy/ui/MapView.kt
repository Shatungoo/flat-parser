package com.helldasy.ui

import androidx.compose.foundation.Image
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
    Row {
        Box(modifier = Modifier.fillMaxWidth(0.7f)) {

            Map(
                points = flats,
                onClick = {
                    selectedFlats.value = it
                },
//            modifier = Modifier.fillMaxWidth(0.7f)
            )
        }
        LazyColumn {
            selectedFlats.value.map { flat ->
                item {
                    Column {
                        Text(flat.dynamic_title.toString(), style = MaterialTheme.typography.h4)
                        Spacer(modifier = Modifier.width(10.dp))
                        Row {
                            Column(modifier = Modifier.width(200.dp)) {
                                textField("Город", flat.city_name.toString())
                                textField("Район", flat.urban_name.toString())
                                textField("Район", flat.district_name.toString())
                                textField("Адрес ", flat.address.toString().toLatin())
                                textField("Цена", flat.price["2"]?.price_total.toString() + " $")
                                textField("Цена за кв.м", flat.price["2"]?.price_square.toString() + " $")
                                textField("Этаж", "${flat.floor.toString()}/${flat.total_floors.toString()}")
                                textField("Комнат", flat.room.toString())
                                textField("Площадь ", flat.area.toString())
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun MapView(settings: Settings) {
    val selectedFlat = mutableStateOf(0)
    val flats = settings.flats
    val selectedFlats = mutableStateOf(emptyList<Response.Flat>())
    Row {
//        Spacer(modifier = Modifier.weight(1f))

        Box {

            LazyColumn(modifier = Modifier.width(400.dp)) {
                item{
                    BackButton(settings.view)
                }
                selectedFlats.value.map { flat ->
                    item {
                        SmallFlatCard(flat, onClick = {
                            settings.selectedFlat.value = flat
                            settings.view.value = Views.Flat
                        })
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

@Composable
fun BackButton(view: MutableState<Views> = mutableStateOf(Views.Main)) {
    OutlinedButton(
        onClick = { settings.view.value = view.value },
        colors = ButtonDefaults.buttonColors(
            backgroundColor = MaterialTheme.colors.onPrimary,
            contentColor = Color.LightGray
        )

    ) {
        Image(
            Icons.Default.ArrowBack,
            contentDescription = "Back",
        )
    }
}
