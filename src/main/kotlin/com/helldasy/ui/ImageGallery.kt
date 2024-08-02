package com.helldasy.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
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
fun ImageGallery(image: SelectedImage) {
    val bitmapImage = mutableStateOf<BitmapPainter?>(null)
    BoxWithConstraints {
        val isBig = maxWidth > 500.dp
        val crop =
            if (isBig) ContentScale.Fit else ContentScale.Crop

        val coroutine = CoroutineScope(Dispatchers.Default).launch {
            bitmapImage.value = getImage(big = isBig, image = image)
        }
        val width = if (isBig) 100.dp else 30.dp
        val boxWidth = maxWidth - width * 2
        Row {
            galleryButton(
                onClick = { image.selectedImage.value-- },
                enabled = image.selectedImage.value > 0,
                text = "<",
                width = width
            )
            Box(
                modifier = Modifier.fillMaxHeight()
                    .width(boxWidth)
                    .align(Alignment.CenterVertically)
            ) {
                bitmapImage.value?.let {
                    Image(
                        it, contentDescription = "",
                        modifier = Modifier.fillMaxHeight()
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

private suspend fun getImage(
    big: Boolean,
    image: SelectedImage,
): BitmapPainter? {
    val selectedImage = image.selectedImage.value
    if (image.images.isNotEmpty()) {
        when {
            big && image.images[selectedImage].large != null -> image.images[selectedImage].large
            image.images[selectedImage].thumb != null -> image.images[selectedImage].thumb
            else -> null
        }?.let { imageUrl ->
            getFile(image.id, imageUrl)?.let { file ->
                val imageBitmap = file.toImageBitmap()
                return BitmapPainter(imageBitmap)
            }
        }
    }
    return null
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
