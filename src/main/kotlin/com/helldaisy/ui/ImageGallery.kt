@file:OptIn(ExperimentalFoundationApi::class)

package com.helldaisy.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.helldaisy.cacheImages
import com.helldaisy.getFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


val cScale = mutableStateOf(ContentScale.Fit)

@Composable
fun SmallImageGallery(
    urls: List<String>,
    id: String,
    selectedImage: MutableState<Int>,
    onClick: () -> Unit,
) {
    if (urls.isEmpty()) return
    val bitmapImage = mutableStateOf<BitmapPainter?>(null)
    bitmapImage.getImage(urls[selectedImage.value], id)
    BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(5.dp)) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick)
        ) {
            bitmapImage.value?.let {
                Image(
                    it,
                    contentDescription = "",
                    modifier = Modifier.align(Alignment.Center).fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } ?: run {
                CircularProgressIndicator()
            }
            GalleryBtns(selectedImage, urls, id, bitmapImage)
        }
    }
}

@Composable
fun CScale() {
    Row {
        Spacer(modifier = Modifier.weight(1f))
        Column {
            Spacer(modifier = Modifier.weight(1f))
            Button(
                modifier = Modifier.width(80.dp).height(40.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Transparent,

                ),

                onClick = {
                    cScale.value = when (cScale.value) {
                        ContentScale.Fit -> ContentScale.Inside
                        else -> ContentScale.Fit
                    }
                }) {
                Text(
                    when (cScale.value) {
                        ContentScale.Crop -> "Inside"
                        else -> "Fit"
                    }, modifier = Modifier.width(60.dp)
                )
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BigImageGallery(
    urls: List<String>,
    id: String,
    selectedImage: MutableState<Int>,
    onClick: () -> Unit = {},
) {
    if (urls.isEmpty()) return
    val bitmapImage = mutableStateOf<BitmapPainter?>(null)
    cacheImages(id, urls)
    bitmapImage.getImage(urls[selectedImage.value], id, 20)
    BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(5.dp)) {
        Box(modifier = Modifier
            .fillMaxSize()
            .onClick { onClick() }) {
            bitmapImage.value?.let {
                Image(
                    it,
                    contentDescription = "",
                    modifier = Modifier.align(Alignment.Center).fillMaxSize(),
                    contentScale = cScale.value
                )
            } ?: run {
                CircularProgressIndicator()
            }
            GalleryBtns(selectedImage, urls, id, bitmapImage, size = 60.dp)
            CScale()
        }
    }
}

fun MutableState<BitmapPainter?>.getImage(
    url: String,
    id: String,
    priority: Int = 10
) {
    val bitmapImage = this
    CoroutineScope(Dispatchers.Default).launch {
        getFile(url = url, imageId = id, priority = priority)
            ?.let { bitmapImage.value = BitmapPainter(it.toImageBitmap()) }
    }
}


@Composable
fun GalleryBtns(
    selectedImage: MutableState<Int>, urls: List<String>, id: String, bitmapImage: MutableState<BitmapPainter?>,
    size: Dp = 40.dp,
) {
    Row {
        galleryButton(
            onClick = {
                if (selectedImage.value > 0)
                    selectedImage.value--
            },
            enabled = selectedImage.value > 0,
            icon = Icons.AutoMirrored.Default.ArrowBack,
            width = size
        )
        Spacer(modifier = Modifier.weight(1f))

        galleryButton(
            onClick = {
                if (selectedImage.value < urls.size - 1) {
                    selectedImage.value += 1
                    bitmapImage.getImage(urls[selectedImage.value], id,20)
                }
            },
            enabled = selectedImage.value < urls.size - 1,
            icon = Icons.AutoMirrored.Default.ArrowForward,
            width = size
        )
    }
}

@Composable
fun galleryButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    icon: ImageVector,
    width: Dp = 30.dp,
) {
    Box(
        modifier = Modifier
            .width(width)
            .fillMaxHeight()
            .clickable(enabled = enabled, onClick = onClick)

    ) {
        CenterH {
            CenterV {
                Image(
                    icon,
                    contentDescription = "Back",
                    modifier = Modifier.shadow(2.dp, shape = RoundedCornerShape(10.dp)),
                )
            }
        }
    }
}

fun ByteArray.toImageBitmap(): ImageBitmap =
    org.jetbrains.skia.Image.makeFromEncoded(this).toComposeImageBitmap()
