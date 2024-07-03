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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.skia.Image
import java.awt.Desktop
import java.net.URI
import java.net.URLEncoder


data class SelectedImage(
    val id: String,
    val images: List<Response.Image>,
    val selectedImage: MutableState<Int>
)

val selectedImage = mutableStateOf<SelectedImage?>(null)
val selectedFlat = mutableStateOf<Response.Flat?>(null)
val db = Db()
val query = mutableStateOf("SELECT * from FLATS order by LAST_UPDATED DESC limit 100")
val flats = mutableStateOf(db.getFlats(query = query.value))

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
                            Desktop.getDesktop().browse(URI.create(telegramUrl))
                        }) {
                            Text("Share in Telegram")
                        }
                    }
                }

            }
        }
    }
}

fun openInBrowser(url: String) {
    Desktop.getDesktop().browse(URI.create(url))
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
    val image = mutableStateOf<BitmapPainter?>(null)

    BoxWithConstraints {

        val big = maxWidth > 500.dp
        if (images.isNotEmpty()) {
            val imageUrl = when {
                big && images[selectedImage.value].large != null -> images[selectedImage.value].large
                images[selectedImage.value].thumb != null -> images[selectedImage.value].thumb
                else -> null
            }
            CoroutineScope(Dispatchers.Default).launch {
                getFile(id, imageUrl)?.let { file ->
                    val imageBitmap = runBlocking {
                        file.readBytes().toImageBitmap()
                    }
                    image.value = BitmapPainter(imageBitmap)
                }
            }
        }
        val width = if (big) 100.dp else 30.dp
        val boxWidth = maxWidth - width * 2
        Row {
            galleryButton(
                onClick = { selectedImage.value-- },
                enabled = selectedImage.value > 0,
                text = "<",
                width = width
            )
            Box(
                modifier = Modifier.fillMaxHeight()
                    .width(boxWidth)
                    .align(Alignment.CenterVertically)
            ) {
                image.value?.let {
                    Image(
                        it, contentDescription = "",
                        modifier = Modifier.fillMaxHeight()
                            .align(Alignment.Center),
                        contentScale = ContentScale.Fit
                    )
                } ?: run{
                    Text("Loading...")
                }
            }
            galleryButton(
                onClick = {
                    if (selectedImage.value < images.size - 1)
                        selectedImage.value += 1
                },
                enabled = selectedImage.value < images.size - 1,
                text = ">",
                width = width
            )
        }
    }
}

@Composable
fun galleryButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    text: String = "",
    width: Dp = 30.dp

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
    Image.makeFromEncoded(this).toComposeImageBitmap()

@Composable
fun ControlPanel(
    view: MutableState<Views>,
    db: Db,
    flats: MutableState<List<Response.Flat>>
) {
    Row(modifier = Modifier.height(50.dp)) {
        val btnName = mutableStateOf("Update DB")
        controlPanelButton(onClick = {
            btnName.value = "In progress..."
            CoroutineScope(Dispatchers.Default).launch {
                for (i in 0..10) {
                    println("page $i")

                    val response = runBlocking { getFlats(i) }
                    db.insertFlats(json.decodeFromString<Response>(response).data.data)
                    flats.value = db.getFlats(query = query.value)
                    btnName.value = "Update DB"
                }
            }
        }, text = btnName.value)


        TextField(
            value = query.value,
            onValueChange = { query.value = it },
//            label = { Text("Query") }
        )
        controlPanelButton(onClick = {
            flats.value = db.getFlats(query = query.value)
        }, text = "Search")
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