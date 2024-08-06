package com.helldasy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.window.singleWindowApplication
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.helldasy.*
import com.helldasy.map.*
import org.jxmapviewer.OSMTileFactoryInfo
import org.jxmapviewer.cache.FileBasedLocalCache
import org.jxmapviewer.viewer.DefaultTileFactory
import org.jxmapviewer.viewer.GeoPosition
import java.nio.file.Paths


val cacheDir = getTemporalDirectory(".osm")
val cache = FileBasedLocalCache(cacheDir, false)
val tileFactory = DefaultTileFactory(OSMTileFactoryInfo())
    .apply {
        setLocalCache(cache)
    }


fun main() = singleWindowApplication {

    val path = Paths.get(settingsPath, "flats").toAbsolutePath().toString()
    val db = Db(path)
    val flats = db.getFlats()
    val selectedFlats = mutableStateOf(emptyList<Response.Flat>())
}


@Composable
fun MapView(
    flats: List<Response.Flat>,
    back: () -> Unit = {},
    selectFlat: (Response.Flat) -> Unit = {},
) {
//    val flats = settings.flats
    val selectedFlats = mutableStateOf(emptyList<Response.Flat>())
    Box {
        val points = flats.mapNotNull {
            if (it.lat != null && it.lng != null) SelectablePoint(GeoPosition(it.lat, it.lng), it)
            else null
        }
        MapCompose(
            centerPoint = points.first().geoPoistion,
            tileFactory = tileFactory,
            zoom = 5,
            layer = ClickableWaypointLayer(points, onClick = { selectedFlats.value = it.map { data -> data as Response.Flat } }),
        )
        Column {
            BackButtonAct { back() }
            LazyColumn(modifier = Modifier.width(400.dp).padding(5.dp).background(MaterialTheme.colors.surface)) {
                item {

                }
                selectedFlats.value.map { flat ->
                    item {
                        SmallFlatCard(flat, onClick = { selectFlat(flat) })
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                }
            }
        }

    }
}

@Composable
fun MapComposeSmall(
    lat: Double = 50.11,
    lng: Double = 8.68,
    zoom: Int = 5,
){
    MapCompose(
        centerPoint = GeoPosition(lat, lng),
        tileFactory = tileFactory,
        zoom = zoom,
    )
}
