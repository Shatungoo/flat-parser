import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import jdk.jfr.Enabled
import kotlinx.coroutines.runBlocking
import org.jetbrains.skia.Image


data class SelectedImage(
    val id: String,
    val images: List<Response.Image>,
    val selectedImage: MutableState<Int>
)

val selectedImage = mutableStateOf<SelectedImage?>(null)
val selectedFlat = mutableStateOf<Response.Flat?>(null)
val db = Db()
val flats = mutableStateOf(db.getFlats())

@Composable
fun MainView(view: MutableState<Views>) {
    val rememberScrollState = rememberScrollState()


    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState)
    ) {
        ControlPanel(view, db, flats)
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
        Box(modifier = Modifier.fillMaxSize()
            .background(Color.Black)
            .clickable(onClick = {
                selectedImage.value = null
            })
        ) {
            ImageGallery(selectedImage.value!!)
        }
    }

}


@Composable
fun FlatCard(
    flat: Response.Flat,
    selectImage: (image: SelectedImage) -> Unit
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
                    textField("Улица", flat.street_id.toString())
                    textField("Цена", flat.price["2"]?.price_total.toString() + " $")
                    textField("Цена за кв.м", flat.price["2"]?.price_square.toString() + " $")
                    textField("Этаж", "${flat.floor.toString()}/${flat.total_floors.toString()}")
                    textField("Комнат", flat.room.toString())
                    textField("Координат", "${flat.lat.toString()}, ${flat.lng.toString()}")
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
                }

            }
        }
    }
}

@Composable
fun FlatView(
    flat: Response.Flat,
    selectImage: (image: SelectedImage) -> Unit
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
fun ImageGallery(image: SelectedImage) {
    val id = image.id
    val images = image.images
    val selectedImage = image.selectedImage
    BoxWithConstraints {
        val big = maxWidth > 500.dp
        Row {
            galleryButton(
                onClick = { selectedImage.value-- },
                enabled = selectedImage.value > 0,
                text = "<",
                wide = big
            )
            Spacer(modifier = Modifier.weight(1f))
            Box(modifier = Modifier.fillMaxHeight().align(Alignment.CenterVertically)) {
                if (images.isNotEmpty()) {
                    val imageUrl = when {
                        big && images[selectedImage.value].large != null -> images[selectedImage.value].large
                        images[selectedImage.value].thumb != null -> images[selectedImage.value].thumb
                        else -> null
                    }
                    getFile(id, imageUrl)?.let { file ->
                        val image = runBlocking {
                            file.readBytes().toImageBitmap()
                        }
                        image.let {
                            val painter = BitmapPainter(it)
                            Image(painter, contentDescription = file.absolutePath)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            galleryButton(
                onClick = {
                    if (selectedImage.value < images.size - 1)
                        selectedImage.value += 1
                },
                enabled = selectedImage.value < images.size - 1,
                text = ">",
                wide = big
            )
        }
    }
}

@Composable
fun galleryButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    text: String = "",
    wide: Boolean = false
) {
    val width = if (wide) 100.dp else 30.dp
    Button(
        modifier = Modifier.width(width).fillMaxHeight(),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = Color.Transparent,
            disabledBackgroundColor = Color.Transparent
        ),
        enabled = enabled,
        onClick = onClick
    ) { Text(text) }
}

fun ByteArray.toImageBitmap(): ImageBitmap =
    Image.makeFromEncoded(this).toComposeImageBitmap()

@Composable
fun ControlPanel(
    view: MutableState<Views>,
    db: Db,
    flats: MutableState<List<Response.Flat>>
) {
    Row(modifier = Modifier.height(50.dp)) {
        controlPanelButton(onClick = {
            for (i in 0..10) {
                println("page $i")
                val response = runBlocking { getFlats(i) }
                json.decodeFromString<Response>(response).data.data.forEach {
                    db.insertFlat(it)
                }
            }
            flats.value = db.getFlats()
        }, text = "Update DB")
    }
}

@Composable
fun controlPanelButton(
    onClick: () -> Unit,
    image: String? = null,
    text: String? = null
) {
    Button(onClick = onClick) {
        if (image != null) Image(painterResource(image), "image")
        if (text != null) Text(text)
    }
}

@Composable
fun filters() {
    Column {
        Text("Price")
    }
}