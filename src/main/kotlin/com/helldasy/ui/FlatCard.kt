package com.helldasy.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.helldasy.Response
import java.net.URI
import java.net.URLEncoder

@Preview
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
            Text(flat.dynamic_title.toString(), style = MaterialTheme.typography.h4)
            Spacer(modifier = Modifier.width(10.dp))
            Row {
                Column(modifier = Modifier.width(200.dp)) {
                    textField("Город", flat.city_name.toString())
                    textField("Район", flat.urban_name.toString())
                    textField("Район", flat.district_name.toString())
                    textField("Адрес ", flat.address.toString().toLatin())
//                    textField("Улица", flat.street_id.toString())
                    textField("Цена", flat.price["2"]?.price_total.toString() + " $")
                    textField("Цена за кв.м", flat.price["2"]?.price_square.toString() + " $")
                    textField("Этаж", "${flat.floor.toString()}/${flat.total_floors.toString()}")
                    textField("Комнат", flat.room.toString())
//                    textField("Координаты", "${flat.lat.toString()}, ${flat.lng.toString()}")
                    textField("Площадь ", flat.area.toString())
                }
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
                    Box(modifier = Modifier.size(300.dp).padding(5.dp), contentAlignment = Alignment.Center) {
                        MapView(flat.lat, flat.lng)
                    }
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
