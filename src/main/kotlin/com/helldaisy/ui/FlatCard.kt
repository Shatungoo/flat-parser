package com.helldaisy.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.unit.dp
import com.helldaisy.Response
import java.net.URI
import java.net.URLEncoder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
fun FlatCard(
    flat: Response.Flat,
    selectImage: (url: List<String>, id: String, selected: MutableState<Int>) -> Unit = { _, _, _ -> },
    selectFlat: () -> Unit = {},
) {
    val image = mutableStateOf(0)

    Row {
        Spacer(modifier = Modifier.weight(1f))
        Card(onClick = selectFlat, backgroundColor = Color.Transparent) {
            Row {
                Box(modifier = Modifier.size(300.dp)) {
                    flat.images.mapNotNull { it.thumb }.let {
                        SmallImageGallery(it, flat.id.toString(), image,
                            onClick = { selectImage(flat.images.mapNotNull { it.large }, flat.id.toString(), image) })
                    }
                }
                Spacer(modifier = Modifier.width(50.dp))
                Column {
                    Text(flat.dynamic_title.toString(), style = MaterialTheme.typography.h4)
                    Spacer(modifier = Modifier.width(10.dp))
                    Row {
                        FlatDescription(flat)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.width(400.dp).fillMaxHeight()) {
                            if (flat.comment != null) TextField(
                                value = flat.comment.toString().replace("<br />", ""),
                                onValueChange = { },
                                readOnly = true,
                            )
                            Spacer(modifier = Modifier.width(50.dp))
                            ShareBtns(flat)
                        }

                        if (flat.lat != null && flat.lng != null) {
                            Spacer(modifier = Modifier.width(10.dp))
                            Box(modifier = Modifier.size(250.dp)) {
                                MapComposeSmall(flat.lat, flat.lng, 4)
                            }
                        }
                    }
                }
            }

        }
        Spacer(modifier = Modifier.weight(1f))
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SmallFlatCard(
    flat: Response.Flat,
    onClick: () -> Unit = {},
) {
    val bitmapImage = mutableStateOf<BitmapPainter?>(null)
    flat.images.firstOrNull()?.thumb?.let {
        bitmapImage.getImage(it, flat.id.toString())
    }
    Card(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Column() {
            Text(flat.dynamic_title.toString(), style = MaterialTheme.typography.h4)
            Row {
                Box(modifier = Modifier.size(100.dp)) {
                    SmallImageGallery(flat.images.mapNotNull { it.thumb }, flat.id.toString(), mutableStateOf(0),
                        onClick = {})
                    bitmapImage.value?.let {
                        Image(
                            it,
                            contentDescription = "",
                            modifier = Modifier.fillMaxHeight().align(Alignment.Center),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                Spacer(modifier = Modifier.width(50.dp))
                FlatDescription(flat, short = true)
            }
        }
    }
}

@Composable
fun FlatDescription(flat: Response.Flat, short: Boolean = false) {
    Column(modifier = Modifier.width(200.dp)) {
        if (!short) textField("Город", flat.city_name.toString())
        if (!short) textField("Район", flat.urban_name.toString())
        if (!short) textField("Район", flat.district_name.toString())
        if (!short) textField("Адрес ", flat.address.toString().toLatin())
        textField("Цена", flat.price["2"]?.price_total.toString() + " $")
        textField("Цена за кв.м", flat.price["2"]?.price_square.toString() + " $")
        textField("Этаж", "${flat.floor.toString()}/${flat.total_floors.toString()}")
        textField("Комнат", flat.room.toString())
        textField("Площадь ", flat.area.toString())
        if (!short) flat.last_updated?.let {
            val ld = LocalDateTime.parse(it, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            val updated = java.time.Duration.between(LocalDateTime.now(), ld).abs()
            if (updated.toDays() > 0) textField("Обновлено", updated.toDays().toString() + " дн назад")
            else if (updated.toHours() > 0) textField("Обновлено", updated.toHours().toString() + " ч назад")
            else textField("Обновлено", updated.toMinutes().toString() + " мин назад")
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
fun FlatCardView(
    flat: Response.Flat,
    back: () -> Unit,
    selectImage: (url: List<String>, id: String, selected: MutableState<Int>) -> Unit = { _, _, _ -> },
) {
    val selectedImage = mutableStateOf(0)
    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            Card {
                BackButtonAct { back() }
                Center {
                    Text(flat.dynamic_title.toString(), style = MaterialTheme.typography.h4)
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 50.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(modifier = Modifier.weight(0.5f)) {
                SmallImageGallery(flat.images.mapNotNull { it.large }, flat.id.toString(), selectedImage,
                    onClick = { selectImage(flat.images.mapNotNull { it.large }, flat.id.toString(), selectedImage) })
            }

            Column(modifier = Modifier.fillMaxHeight().weight(0.5f).defaultMinSize(300.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Row(modifier = Modifier) {
                    FlatDescription(flat)

                    if (flat.lat != null && flat.lng != null) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(modifier = Modifier.size(250.dp).padding(5.dp), contentAlignment = Alignment.Center) {
                            MapComposeSmall(flat.lat, flat.lng, 4)
                        }
                    }
                }

                ShareBtns(flat)
                if (flat.comment != null) TextField(
                    value = flat.comment.toString().replace("<br />", ""),
                    onValueChange = { },
                    readOnly = true,
                )
            }

        }
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun ShareBtns(flat: Response.Flat) {
    Row(modifier = Modifier.height(40.dp), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Button(
            modifier = Modifier.fillMaxHeight(),
            onClick = { openInBrowser("https://www.myhome.ge/ru/pr/${flat.id}/details/") }) {
            Text("Open")
//        Image(
//            Icons.Default.,
//            contentDescription = "Back",
//        )
        }
        Button(modifier = Modifier.fillMaxHeight(), onClick = {
            val url = "https://www.myhome.ge/ru/pr/${flat.id}/details/"
            val text = flat.dynamic_title.toString()
            val encodedUrl = URI.create(url).toASCIIString()
            val encodedText = URLEncoder.encode(text, "UTF-8")
            val telegramUrl = "https://t.me/share/url?url=$encodedUrl&text=$encodedText"
            openInBrowser(telegramUrl)
        }) {
            Image(
                Icons.AutoMirrored.Default.Send,
                contentDescription = "Back",
            )
        }
    }
}

@Composable
fun Center(content: @Composable () -> Unit) {
    Row {
        Spacer(modifier = Modifier.weight(1f))
        content()
        Spacer(modifier = Modifier.weight(1f))
    }
}


@Composable
fun BackButtonAct(back: () -> Unit) {
    val focus = mutableStateOf(false)
    OutlinedButton(
        onClick = back,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = MaterialTheme.colors.onPrimary,
            contentColor = if (focus.value) Color.LightGray else Color.Red
        ),
        modifier = Modifier.onPlaced { focus.value = !focus.value }
    ) {
        Image(
            Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
        )
    }
}
