package com.helldasy.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.helldasy.Response
import com.helldasy.Settings
import com.helldasy.Views
import com.helldasy.updateDb
import java.awt.event.ItemEvent


data class SelectedImage(
    val id: String,
    val images: List<Response.Image>,
    val selectedImage: MutableState<Int>,
)

val selectedImage = mutableStateOf<SelectedImage?>(null)
val filterView = mutableStateOf(false)
val filterParserView = mutableStateOf(false)

@Composable
fun MainView(settings: Settings) {
    val rememberScrollState = rememberScrollState()

    val flats = settings.flats
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState)
    ) {
        ControlPanel(settings)
        Spacer(modifier = Modifier.height(10.dp))
        Box(modifier = Modifier.fillMaxWidth().weight(1f).align(Alignment.CenterHorizontally)) {
            LazyColumn {
                items(flats.value) { flat ->
                    FlatCard(flat = flat, selectImage = { image ->
                        selectedImage.value = image
                    }, onClick = {
                        settings.selectedFlat.value = flat
                        settings.view.value = Views.Flat
                    })
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
    if (selectedImage.value != null) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black).clickable(onClick = {
                    selectedImage.value = null
                })
        ) {
            ImageGallery(selectedImage.value!!)
        }
    } else if (filterView.value) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Row {
                Box(
                    modifier = Modifier.fillMaxHeight().background(Color.Black).width(400.dp)
                ) {
                    FilterDb(settings.filterDb) {
                        settings.flats.value = settings.db.getFlats(settings.filterDb.value)
                    }
                }
                Box(
                    modifier = Modifier.fillMaxSize().clickable(onClick = {
                            filterView.value = false
                        })
                )
            }
        }
    } else if (filterParserView.value) {
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
}

@Composable
fun FlatView(
    settings: Settings
) {
    val flat = settings.selectedFlat.value!!

    val image = SelectedImage(flat.id.toString(), flat.images, mutableStateOf(0))
    Row {

        Box(modifier = Modifier.size(300.dp).clickable(onClick = {
//            selectImage(image)
        })) {
            ImageGallery(image)
        }
        Spacer(modifier = Modifier.width(50.dp))
        Column {
            Text(
                flat.dynamic_title.toString(),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Row {
                Column {
                    textField("Город", flat.city_name.toString())
                    textField("Район", flat.urban_name.toString())
                    textField("Район", flat.district_name.toString())
                    textField("Адрес ", flat.address.toString())
                    textField("Улица", flat.street_id.toString())
                    textField("Цена", flat.price["2"]?.price_total.toString() + " $")
                    textField("Цена за кв.м", flat.price["2"]?.price_square.toString() + " $")
                    textField("Этаж", "${flat.floor.toString()}/${flat.total_floors.toString()}")
                    textField("Комнат", flat.room.toString())
                    textField("Координаты", "${flat.lat.toString()}, ${flat.lng.toString()}")
                    textField("Площадь ", flat.area.toString())
                }
                Column {
                    TextField(value = flat.comment.toString().replace("<br />", ""),
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Label") })
                }

            }
        }
    }
}



@Composable
fun ControlPanel(
    settings: Settings,
) {
    val db = settings.db
    val flats = settings.flats
    Card {
        Row(
            modifier = Modifier.height(40.dp), horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            val btnName = mutableStateOf("Update DB")
            BtnWithSettings(name = btnName, action = {
                btnName.value = "In progress..."
                updateDb(settings) {
                    settings.flats.value = db.getFlats(settings.filterDb.value)
                    btnName.value = "Update DB"
                }
            }, settings = {
                filterParserView.value = true
            })
            controlPanelButton(onClick = {
                filterView.value = true
            }, text = "Filter")

            controlPanelButton(onClick = {
                settings.flats.value = db.getFlats(settings.filterDb.value)
            }, text = "Search")
            controlPanelButton(onClick = {
                settings.view.value = Views.Map
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

