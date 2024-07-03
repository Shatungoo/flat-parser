import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.jetbrains.skia.Image
import kotlinx.coroutines.runBlocking

@Composable
fun MainView(view: MutableState<Views>) {
    val db = Db()
    val flats = db.getFlats().subList(0, 5)
    Row {
        ControlPanel(view)
        Column {
            flats.forEach {
                FlatCard(it)
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}


@Composable
fun FlatCard(flat: Response.Flat) {
    Row {
        Column {
            Row() {
                val selectedImage = mutableStateOf(0)
                ImageGallery(flat.id.toString(), flat.images, selectedImage)
                Column {
                    Text(flat.dynamic_title.toString())

                    Row {
                        Column {
                            Text("city name: " + flat.city_name.toString())
                            Text("urban name: " + flat.urban_name.toString())
                            Text("district name: " + flat.district_name.toString())
                            Text("address: " + flat.address.toString())
                            Text("street: " + flat.street_id.toString())
                            Text("price: " + flat.price["2"]?.price_total.toString())
                            Text("price per square: " + flat.price["2"]?.price_square.toString())
                            Text("floor/total: ${flat.floor.toString()}/${flat.total_floors.toString()}")
                            Text("room: " + flat.room.toString())
                            Text("Coordinates: ${flat.lat.toString()}, ${flat.lng.toString()}")
                            Text("area: " + flat.area.toString())
                        }
                        Column {
                            Text("Comments: ")
                            Text(flat.comment.toString())
                        }

                    }
                }
            }
        }
    }

}

@Composable
fun ImageGallery(id: String, images: List<Response.Image>, selectedImage: MutableState<Int>) {
    Box(modifier = Modifier.size(300.dp)) {
        Row {
            Button(
                modifier = Modifier.width(30.dp).fillMaxHeight(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
                enabled = selectedImage.value > 0,
                onClick = {
                    selectedImage.value--
                    println(selectedImage.value)
                }) { Text("<") }
            Spacer(modifier = Modifier.weight(1f))
            Box(modifier = Modifier.fillMaxHeight()) {
                getFile(id, images[selectedImage.value].thumb!!)?.let { file ->
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
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
                enabled = selectedImage.value < images.size - 1,
                onClick = {
                    println(selectedImage.value)
                    selectedImage.value += 1
                    println(selectedImage.value)
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
    image: String
) {
    Button(onClick = onClick) {
        Image(painterResource(image), "image")
    }
}