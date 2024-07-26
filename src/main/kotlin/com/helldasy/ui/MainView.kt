package com.helldasy.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.helldasy.Response
import com.helldasy.Settings
import com.helldasy.getFlats
import com.helldasy.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.net.URI
import java.net.URLEncoder


data class SelectedImage(
    val id: String,
    val images: List<Response.Image>,
    val selectedImage: MutableState<Int>,
)

val selectedImage = mutableStateOf<SelectedImage?>(null)
val filterView = mutableStateOf<Boolean>(false)

@Composable
fun MainView(settings: Settings) {
    val rememberScrollState = rememberScrollState()

    val flats = settings.flats
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState)
    ) {
        ControlPanel(settings)
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            LazyColumn {
                items(flats.value) { flat ->
                    FlatCard(flat = flat,
                        selectImage = { image ->
                            selectedImage.value = image
                        })
                    Spacer(modifier = Modifier.height(50.dp))
                }
            }
        }
    }
    if (selectedImage.value != null) {
        Box(
            modifier = Modifier.fillMaxSize()
                .background(Color.Black)
                .clickable(onClick = {
                    selectedImage.value = null
                })
        ) {
            ImageGallery(selectedImage.value!!)
        }
    }
    if (filterView.value) {
        Box(
            modifier = Modifier.fillMaxSize()
                .background(Color.Black)
        ) {
            Filters(settings.filters)
        }
    }
}

@Composable
fun FlatCard(
    flat: Response.Flat,
    selectImage: (image: SelectedImage) -> Unit,
) {
    val image = SelectedImage(flat.id.toString(), flat.images, mutableStateOf(0))
    Row {

        Box(modifier = Modifier.size(300.dp).clickable(onClick = {
            selectImage(image)
        })) {
            ImageGallery(image)
        }
        Spacer(modifier = Modifier.width(50.dp))
        Column {
            Text(flat.dynamic_title.toString())
            Spacer(modifier = Modifier.width(10.dp))
            Row {
                Column(modifier = Modifier.width(200.dp))
                {
                    textField("Город", flat.city_name.toString())
                    textField("Район", flat.urban_name.toString())
                    textField("Район", flat.district_name.toString())
                    textField("Адрес ", flat.address.toString())
//                    textField("Улица", flat.street_id.toString())
                    textField("Цена", flat.price["2"]?.price_total.toString() + " $")
                    textField("Цена за кв.м", flat.price["2"]?.price_square.toString() + " $")
                    textField("Этаж", "${flat.floor.toString()}/${flat.total_floors.toString()}")
                    textField("Комнат", flat.room.toString())
                    textField("Координаты", "${flat.lat.toString()}, ${flat.lng.toString()}")
                    textField("Площадь ", flat.area.toString())
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    if (flat.comment != null)
                        TextField(
                            value = flat.comment.toString().replace("<br />", ""),
                            onValueChange = { },
                            readOnly = true,
                        )
                    Spacer(modifier = Modifier.weight(0.1f))
                    Row {
                        Button(onClick = { openInBrowser("https://www.myhome.ge/ru/pr/${flat.id}/details/") }) {
                            Text("Open")
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(onClick = {
                            val url = "https://www.myhome.ge/ru/pr/${flat.id}/details/"
                            val text = flat.dynamic_title.toString()
                            val encodedUrl = URI.create(url).toASCIIString()
                            val encodedText = URLEncoder.encode(text, "UTF-8")
                            val telegramUrl = "https://t.me/share/url?url=$encodedUrl&text=$encodedText"
                            openInBrowser(telegramUrl)
                        }) {
                            Text("Share in Telegram")
                        }
                    }
                }

            }
        }
    }
}

@Composable
fun FlatView(
    flat: Response.Flat,
    selectImage: (image: SelectedImage) -> Unit,
) {
    val image = SelectedImage(flat.id.toString(), flat.images, mutableStateOf(0))
    Row {

        Box(modifier = Modifier.size(300.dp).clickable(onClick = {
            selectImage(image)
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
                    TextField(
                        value = flat.comment.toString().replace("<br />", ""),
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Label") }
                    )
                }

            }
        }
    }
}

@Composable
fun textField(label: String, value: String) {
    Row {
        Text(text = "$label:", modifier = Modifier.width(100.dp))
        Text(value)
    }
}

@Composable
fun ControlPanel(
    settings: Settings,
) {
    val db = settings.db
    val flats = settings.flats
    Row(modifier = Modifier.height(50.dp)) {
        val btnName = mutableStateOf("Update DB")
        controlPanelButton(onClick = {
            btnName.value = "In progress..."
            CoroutineScope(Dispatchers.Default).launch {
                for (i in 0..10) {
                    println("page $i")

                    val response = runBlocking { getFlats(i) }
                    db.insertFlats(json.decodeFromString<Response>(response).data.data)
                    flats.value = db.getFlats(query = settings.filters.value.buildQuery())
                    btnName.value = "Update DB"
                }
            }
        }, text = btnName.value)

//        TextField(
//            value = settings.filters.value.buildQuery(),
//            readOnly = true,
//            onValueChange = {
//                println("textfield "+settings.filters.value.buildQuery()+" " + it)
//                it =settings.filters.value.buildQuery()},
//        )
        controlPanelButton(onClick = {
            flats.value = db.getFlats(query = settings.filters.value.buildQuery())
        }, text = "Search")
        controlPanelButton(onClick = {
            filterView.value = true
        }, text = "Filter")
    }
}

@Composable
fun controlPanelButton(
    onClick: () -> Unit,
    image: String? = null,
    text: String? = null,
) {
    Button(onClick = onClick) {
        if (image != null) Image(painterResource(image), "image")
        if (text != null) Text(text)
    }
}

