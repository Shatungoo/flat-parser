package com.helldasy.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.helldasy.Response
import com.helldasy.Views
import com.helldasy.getFile
import com.helldasy.map.Map
import com.helldasy.settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.net.URI
import java.net.URLEncoder

@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
fun FlatCard(
    flat: Response.Flat,
    selectImage: (image: SelectedImage) -> Unit,
    onClick: () -> Unit = {},
) {
    val image = SelectedImage(flat.id.toString(), flat.images, mutableStateOf(0))

    Row {
        Spacer(modifier = Modifier.weight(1f))
        Card(onClick = onClick, backgroundColor = Color.Transparent) {
            Row {
                Box(modifier = Modifier.size(300.dp).clickable(onClick = {
                    selectImage(image)
                })) {
                    ImageGallery(image)
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
                            Spacer(modifier = Modifier.weight(1f))
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

                        if (flat.lat != null && flat.lng != null) {
                            Spacer(modifier = Modifier.width(10.dp))
                            Box(modifier = Modifier.size(250.dp).padding(5.dp), contentAlignment = Alignment.Center) {
                                Map(flat.lat, flat.lng, visibility = mutableStateOf(selectedImage.value == null))
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
    val bitmapImage = runBlocking {
        flat.images[0].thumb?.let { link ->
            getFile(flat.id.toString(), link)?.let { file ->
                return@runBlocking BitmapPainter(file.toImageBitmap()) as Painter
            }
        }
        return@runBlocking null
    }
    Card(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Column() {
            Text(flat.dynamic_title.toString(), style = MaterialTheme.typography.h4)
            Row {
                Box(modifier = Modifier.size(100.dp).clickable(onClick = {})) {
//            bitmapImage.value?.let {
                    bitmapImage?.let {
                        Image(
                            bitmapImage,
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
    selectImage: (image: SelectedImage) -> Unit = {},
) {
    val image = SelectedImage(flat.id.toString(), flat.images, mutableStateOf(0))
    Column {
        BackButtonAct { back() }
        Center {
            Text(flat.dynamic_title.toString(), style = MaterialTheme.typography.h3)
        }
        Center {
            Box(modifier = Modifier.size(500.dp).clickable(onClick = {
                selectImage(image)
            })) {
                ImageGallery(image)
            }
        }
        Spacer(modifier = Modifier.width(50.dp))
        Center {
            Column {
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
                        Spacer(modifier = Modifier.weight(1f))
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

                    if (flat.lat != null && flat.lng != null) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(modifier = Modifier.size(250.dp).padding(5.dp), contentAlignment = Alignment.Center) {
                            Map(flat.lat, flat.lng, visibility = mutableStateOf(selectedImage.value == null))
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
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
    OutlinedButton(
        onClick = back,
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
