package com.helldasy.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
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
import com.helldasy.getFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ImageGallery(
    image: SelectedImage,
    close: () -> Unit = {}
) {
    val bitmapImage = mutableStateOf<BitmapPainter?>(null)
    val (large, thumb) = image.images[image.selectedImage.value]

    BoxWithConstraints {
        val isBig = maxWidth > 700.dp
        val crop =
            if (isBig) ContentScale.Fit else ContentScale.Crop

        when {
            isBig -> large
            else -> thumb
        }?.let {
            CoroutineScope(Dispatchers.Default).launch {
                bitmapImage.value = getFile(url = it, imageId = image.id)
                    ?.let { BitmapPainter(it.toImageBitmap()) }
            }
        }
        val width = if (isBig) 100.dp else 30.dp
        val boxWidth = maxWidth - width * 2
        Row {
            galleryButton(
                onClick = {
                    if (image.selectedImage.value > 0) image.selectedImage.value--
                },
                enabled = image.selectedImage.value > 0,
                text = "<",
                width = width
            )
            Box(
                modifier = Modifier.height(this@BoxWithConstraints.maxHeight - 10.dp)
                    .width(boxWidth)
                    .align(Alignment.CenterVertically)
            ) {
                bitmapImage.value?.let {
                    Image(
                        it,
                        contentDescription = "",
                        modifier = Modifier
                            .align(Alignment.Center),
                        contentScale = crop
                    )
                } ?: run {
                    CircularProgressIndicator()
                }
            }

            galleryButton(
                onClick = {
                    if (image.selectedImage.value < image.images.size - 1)
                        image.selectedImage.value += 1
                },
                enabled = image.selectedImage.value < image.images.size - 1,
                text = ">",
                width = width
            )
        }
    }
}


@Composable
fun SmallImageGallery(
    urls: List<String>,
    id: String,
    selectedImage: MutableState<Int>
) {
//    val selectedImage = mutableStateOf(0)
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
                width = 30.dp
            )
            Box(modifier = Modifier.align(Alignment.CenterVertically).width(this@BoxWithConstraints.maxWidth - 60.dp)) {
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
                width = 30.dp
            )
        }
    }
}


@Composable
fun BigImageGallery(
    urls: List<String>,
    id: String,
    selectedImage: MutableState<Int>
) {
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
            Box(modifier = Modifier.align(Alignment.CenterVertically).width(this@BoxWithConstraints.maxWidth - 120.dp)) {
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

private fun MutableState<BitmapPainter?>.getImage(
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
