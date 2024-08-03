package com.helldasy.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.helldasy.Response
import com.helldasy.Settings
import com.helldasy.Views
import com.helldasy.updateDb


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
//                        settings.selectedFlat.value = flat
//                        settings.view.value = Views.Flat
                    })
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
    (selectedImage.value)?.let {
        val (id, images) = it
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black)
                .clickable(onClick = { selectedImage.value = null })
        ) {
//            ImageGallery(selectedImage.value!!)
            BigImageGallery(images.mapNotNull { it.large }, id, it.selectedImage)
        }
    }
    if (filterView.value) {
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

