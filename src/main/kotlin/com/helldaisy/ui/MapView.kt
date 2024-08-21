package com.helldaisy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.window.singleWindowApplication
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.helldaisy.*
import com.helldaisy.map.*
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
}


@Composable
fun MapView(
    flats: List<Response.Flat>,
    back: () -> Unit = {},
    selectFlat: (Response.Flat) -> Unit = {},
) {
    Box {
        val flatMap = mutableMapOf<GeoPosition, List<Response.Flat>>()
        flats.mapNotNull {
            if (it.lat != null && it.lng != null) Pair(GeoPosition(it.lat, it.lng), it)
            else null
        }.forEach { (geo, flat) ->
            val flatList = flatMap[geo]
            if (flatList == null) {
                flatMap[geo] = listOf(flat)
            } else if (flatMap[geo] != null) {
                flatMap[geo] = flatList + flat
            }
        }
        val selectedFlats = mutableStateOf(emptyList<Response.Flat>())
        val center = MapData.getCenter(flatMap.keys.toList())
        val selectableWaypoints = flatMap.map { (geo, flatList) ->
            SelectablePoint(
                geoPoistion = geo,
                data = flatList,
            )
        }
        MapData.create(tileFactory, center, 6).Map {
            ClickableWaypointLayer(
                selectablePoints = selectableWaypoints,
                onClick = { selectedFlats.value = it.map { data -> data as List<Response.Flat> }.flatten() })
        }
        Column {
            BackButtonAct { back() }
            LazyColumn(
                modifier = Modifier//.width(400.dp)
                    .padding(5.dp).background(MaterialTheme.colors.surface),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                selectedFlats.value.map { flat ->
                    item { SmallFlatCard(flat, onClick = { selectFlat(flat) }) }
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
) {
    MapData.create(tileFactory, GeoPosition(lat, lng), zoom).Map {
        WaypointsLayer(GeoPosition(lat, lng))
        MoveableLayer()
    }
}
