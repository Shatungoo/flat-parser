import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
//        Image(painterResource("flat.png"), "flat")
        Column {
            Row() {
                Box(modifier = Modifier.size(300.dp)) {
                    flat.images[0].thumb?.let {
                        if (it.contains("static")) return@let
                        val file = getFile(flat.id.toString(), it)
                        val image = runBlocking {
                            file.readBytes().toImageBitmap()
                        }
                        println("image: ${file.absolutePath}")
                        val painter = BitmapPainter(image)
                        Image(painter, contentDescription = file.absolutePath)
                    }
                }
                Column {
                    Text("id: " + flat.id.toString())
                    Text("price: " + flat.price["2"]?.price_total.toString())
                    Text("price per square: " + flat.price["2"]?.price_square.toString())
                    Text("street id: " + flat.street_id.toString())
                    Text("floor/total: ${flat.floor.toString()}/${flat.total_floors.toString()}")
                    Text("room: " + flat.room.toString())
                    Text("Coordinates: ${flat.lat.toString()}, ${flat.lng.toString()}")
                    Text("area: " + flat.area.toString())
                    Text("address: " + flat.address.toString())
                    Text("city name: " + flat.city_name.toString())
                    Text("district name: " + flat.district_name.toString())
                }
            }
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