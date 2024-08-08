package com.helldaisy.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.onClick
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.helldaisy.getFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Composable
fun SmallImageGallery(
    urls: List<String>,
    id: String,
    selectedImage: MutableState<Int>,
    onClick: () -> Unit
) {
    if (urls.isEmpty()) return
    val bitmapImage = mutableStateOf<BitmapPainter?>(null)
     bitmapImage.getImage(urls[selectedImage.value], id)
    BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(5.dp)) {
        Row {
            galleryButton(
                onClick = {
                    if (selectedImage.value > 0) selectedImage.value--
                },
                enabled = selectedImage.value > 0,
                text = "<",
                width = 30.dp
            )
            Box(modifier = Modifier.align(Alignment.CenterVertically)
                .width(this@BoxWithConstraints.maxWidth - 60.dp)
                .clickable(onClick= onClick)) {
                bitmapImage.value?.let {
                    Image(
                        it,
                        contentDescription = "",
                        modifier = Modifier.align(Alignment.Center),
                        contentScale = ContentScale.Crop
                    )
                } ?: run {
                    CircularProgressIndicator()
                }
            }

            galleryButton(
                onClick = {
                    if (selectedImage.value < urls.size - 1) {
                        selectedImage.value += 1
                    }
                },
                enabled = selectedImage.value < urls.size - 1,
                text = ">",
                width = 30.dp
            )
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BigImageGallery(
    urls: List<String>,
    id: String,
    selectedImage: MutableState<Int>,
    onClick: () -> Unit = {}
) {
    if (urls.isEmpty()) return
    val bitmapImage = mutableStateOf<BitmapPainter?>(null)
    bitmapImage.getImage(urls[selectedImage.value], id)
    BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(5.dp)) {
        Row {
            galleryButton(
                onClick = {
                    if (selectedImage.value > 0)
                        selectedImage.value--
                },
                enabled = selectedImage.value > 0,
                text = "<",
                width = 60.dp
            )
            Box(modifier = Modifier.align(Alignment.CenterVertically).width(this@BoxWithConstraints.maxWidth - 120.dp)
                .onClick { onClick() }) {
                bitmapImage.value?.let {
                    Image(
                        it,
                        contentDescription = "",
                        modifier = Modifier.align(Alignment.Center),
                        contentScale = ContentScale.Crop
                    )
                } ?: run {
                    CircularProgressIndicator()
                }
            }

            galleryButton(
                onClick = {
                    if (selectedImage.value < urls.size - 1) {
                        selectedImage.value += 1
                        bitmapImage.getImage(urls[3], id)
                    }
                },
                enabled = selectedImage.value < urls.size - 1,
                text = ">",
                width = 60.dp
            )
        }
    }
}

fun MutableState<BitmapPainter?>.getImage(
    url: String,
    id: String,
) {
    val bitmapImage = this
    CoroutineScope(Dispatchers.Default).launch {
        getFile(url = url, imageId = id)
            ?.let { bitmapImage.value = BitmapPainter(it.toImageBitmap()) }
    }
}


@Composable
fun galleryButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    text: String = "",
    width: Dp = 30.dp,
) {
    Button(
        modifier = Modifier.width(width)
            .fillMaxHeight(),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = Color.Transparent,
            disabledBackgroundColor = Color.Transparent
        ),
        enabled = enabled,
        onClick = onClick
    ) { Text(text) }
}

fun ByteArray.toImageBitmap(): ImageBitmap =
    org.jetbrains.skia.Image.makeFromEncoded(this).toComposeImageBitmap()
