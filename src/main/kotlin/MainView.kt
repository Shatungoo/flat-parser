import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun MainView(view: MutableState<Views>) {
    val db = Db()
    val flats = db.getFlats()
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
//                flat.images.get(1).thumb?.let { painterResource(it) }
                Text("id:" + flat.id.toString())
                Text("price: " + flat.price["2"]?.price_total.toString())
                Text("price square: " + flat.price["2"]?.price_square.toString())
                Text("street id: " + flat.street_id.toString())
                Text("total floors: " + flat.total_floors.toString())
                Text("floor/total: ${flat.floor.toString()}/${flat.total_floors.toString()}")
                Text("room: " + flat.room.toString())
                Text("lat: " + flat.lat.toString())
                Text("lng: " + flat.lng.toString())
                Text("area: " + flat.area.toString())
                Text("address: " + flat.address.toString())
                Text("city name: " + flat.city_name.toString())
                Text("district name: " + flat.district_name.toString())
            }
    }

}

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
    Button(onClick = onClick,) {
        Image(painterResource(image), "image")
    }
}