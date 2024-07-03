import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.runBlocking
import org.jetbrains.skia.Image

@Composable
fun MainView(view: MutableState<Views>) {
    val db = Db()
    val flats = db.getFlats().toMutableList()

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
    ) {
        ControlPanel(view)
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            LazyColumn {
                items(flats) { flat ->
                    FlatCard(flat)
                    Spacer(modifier = Modifier.height(50.dp))
                }
            }
        }
    }
}


@Composable
fun FlatCard(flat: Response.Flat) {
    Row() {
        val selectedImage = mutableStateOf(0)
        ImageGallery(flat.id.toString(), flat.images, selectedImage)
        Spacer(modifier = Modifier.width(50.dp))
        Column {
            Text(
                flat.dynamic_title.toString(),
//                        style = MaterialTheme.typography.h2
            )
            Spacer(modifier = Modifier.width(10.dp))
            Row {
                Column {
                    textField("Город", flat.city_name.toString())
                    textField("Район", flat.urban_name.toString())
                    textField("Район", flat.district_name.toString())
                    textField("Адрес ", flat.address.toString())
                    textField("Улица", flat.street_id.toString())
                    textField("Цена", flat.price["2"]?.price_total.toString()+ " $")
                    textField("Цена за кв.м", flat.price["2"]?.price_square.toString() + " $")
                    textField("Этаж", "${flat.floor.toString()}/${flat.total_floors.toString()}")
                    textField("Комнат", flat.room.toString())
                    textField("Координат", "${flat.lat.toString()}, ${flat.lng.toString()}")
                    textField("Площадь ", flat.area.toString())
                }
                Column {
                    Text("Comments: ")
                    Text(flat.comment.toString())
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
fun ImageGallery(id: String, images: List<Response.Image>, selectedImage: MutableState<Int>) {
    Box(modifier = Modifier.size(300.dp)) {
        Row {
            Button(
                modifier = Modifier.width(30.dp).fillMaxHeight(),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Transparent,
                    disabledBackgroundColor = Color.Transparent
                ),
                enabled = selectedImage.value > 0,
                onClick = { selectedImage.value-- }) { Text("<") }
            Spacer(modifier = Modifier.weight(1f))
            Box(modifier = Modifier.fillMaxHeight()) {
                if (images.isNotEmpty())
                    getFile(id, images[selectedImage.value].thumb)?.let { file ->
                        val image = runBlocking {
                            file.readBytes().toImageBitmap()
                        }
                        println("f${selectedImage.value}: ${file.absolutePath}")
                        image.let {
                            val painter = BitmapPainter(it)
                            Image(painter, contentDescription = file.absolutePath)
                        }
                    }
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(
                modifier = Modifier.width(30.dp).fillMaxHeight(),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Transparent,
                    disabledBackgroundColor = Color.Transparent
                ),
                enabled = selectedImage.value < images.size - 1,
                onClick = {
                    if (selectedImage.value < images.size - 1)
                        selectedImage.value += 1
                }) { Text(">") }
        }

    }
}

fun ByteArray.toImageBitmap(): ImageBitmap =
    Image.makeFromEncoded(this).toComposeImageBitmap()

@Composable
fun ControlPanel(view: MutableState<Views>) {
    Row(modifier = Modifier.height(50.dp)) {
        controlPanelButton(onClick = { view.value = Views.Settings }, "settings.png")
        controlPanelButton(onClick = { view.value = Views.Settings }, "settings.png")
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