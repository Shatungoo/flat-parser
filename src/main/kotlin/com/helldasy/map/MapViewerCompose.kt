package com.helldasy.map

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.helldasy.getTemporalDirectory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.skia.Canvas
import org.jxmapviewer.OSMTileFactoryInfo
import org.jxmapviewer.cache.FileBasedLocalCache
import org.jxmapviewer.viewer.DefaultTileFactory
import org.jxmapviewer.viewer.GeoPosition
import org.jxmapviewer.viewer.TileFactory
import java.awt.geom.Point2D
import java.awt.image.BufferedImage
import kotlin.math.floor

data class Point(val x: Double, val y: Double){
    operator fun minus(other: Point) = Point(x - other.x, y - other.y)
    operator fun plus(other: Point) = Point(x + other.x, y + other.y)

    operator fun minus(other: Offset) = Point(x - other.x, y - other.y)
    operator fun plus(other: Offset) = Point(x + other.x, y + other.y)

    operator fun times(other: Double) = Point(x * other, y * other)
    operator fun div(other: Double) = Point(x / other, y / other)

    operator fun div(other: Point) = Point(x / other.x, y / other.y)
    operator fun div(other: Int) = Point(x / other, y / other)

    fun toOffset() = Offset(x.toFloat(), y.toFloat())
//    operator fun rangeTo(other: Point):  = IntRange(x.toInt(), other.x.toInt())
}
//data class Waypoint(val position: GeoPosition, val label: String)

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun MapCompose(
    tileFactory: TileFactory,
    centerPoint :GeoPosition = GeoPosition(42.50, 43.00),
    waypoints: MutableState<List<GeoPosition>> = mutableStateOf(emptyList<GeoPosition>()),
    overlay: @Composable (Canvas) -> Unit = { }

) {
    var zoomLevel by remember { mutableStateOf(8) }
    val loadingImage = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).toComposeImageBitmap()
    val a = tileFactory.geoToPixel(centerPoint, zoomLevel)
    var center by remember { mutableStateOf(Point(a.x,a.y)) }


//    fun setAddressLocation(addressLocation: GeoPosition) {
//        val old: GeoPosition = this.getAddressLocation()
//        this.addressLocation = addressLocation
//        this.setCenter(this.getTileFactory().geoToPixel(addressLocation, this.getZoom()))
//        this.firePropertyChange("addressLocation", old, this.getAddressLocation())
//        this.repaint()
//    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { c, pan, _, _ ->
//                    center -= pan
                    println(c)
                    center -= pan
                }
            }
            .onPointerEvent(PointerEventType.Scroll) {
                val zoom = it.changes.first().scrollDelta.y
                val oldMapSize = tileFactory.getMapSize(zoomLevel)
                zoomLevel = (zoomLevel + zoom).toInt().coerceIn(
                    tileFactory.info.minimumZoomLevel,
                    tileFactory.info.maximumZoomLevel
                )
                val mapSize = tileFactory.getMapSize(zoomLevel)
                val oldCenter = center
                center = Point(
                    oldCenter.x * (mapSize.getWidth() / oldMapSize.getWidth()),
                    oldCenter.y * (mapSize.getHeight() / oldMapSize.getHeight())
                )
            },
    ) {
        val tileSize = tileFactory.getTileSize(zoomLevel)
        val topLeft = Point(center.x - size.width / 2, center.y - size.height / 2)

        val startTileX = floor(topLeft.x / tileSize).toInt() // номер тайла
        val startTileY = floor(topLeft.y / tileSize).toInt()

        val numWide = (this.size.width / tileSize).toInt() + 2 // количество тайлов по ширине
        val numHigh = (this.size.height / tileSize).toInt() + 2
        drawIntoCanvas { canvas ->
            for (itpx in startTileX.rangeTo(startTileX + numWide))
                        for (itpy in startTileY..(startTileY+numHigh)) {
                val ox = itpx * tileSize - topLeft.x // координаты тайла на экране
                val oy = itpy * tileSize - topLeft.y

                val image = mutableStateOf(loadingImage)
                tileFactory.getTile(itpx, itpy, zoomLevel)?.let {
                    var tile = it
                    if (tile.isLoaded) {
                        image.value = tile.image.toComposeImageBitmap()
                    } else CoroutineScope(Dispatchers.Default).launch {
                        while (!tile.isLoaded or tile.loadingFailed()) {
                            delay(100)
                            tile = tileFactory.getTile(itpx, itpy, zoomLevel)
                        }
                        image.value = tile.image.toComposeImageBitmap()
                    }
                }
                canvas.drawImage(
                    image.value,
                    Offset(ox.toFloat(), oy.toFloat()),
                    Paint()
                )

            }

            val point = tileFactory.geoToPixel(centerPoint, zoomLevel).toPoint()

            val topLeft = Point(center.x - size.width / 2, center.y - size.height / 2)

            val offset= (point - topLeft).toOffset()

            canvas.drawCircle(offset, 5f, Paint().apply { color = Color.Red })
//            waypoints.value.forEach { waypoint ->
//                val point1 = tileFactory.geoToPixel(waypoint, zoomLevel)
//                val offset = Offset(
//                    (point1.x - center.x).toFloat(),
//                    (point1.y - center.y).toFloat()
//                )
//
//                canvas.drawCircle(offset, 5f, Paint().apply { color = androidx.compose.ui.graphics.Color.Red })
//            }
        }
    }
}

private fun Point2D.toPoint(): Point {
    return Point(x, y)
}


@Preview
@Composable
fun PreviewMapViewer() {
    val cacheDir = getTemporalDirectory(".osm")
    val cache = FileBasedLocalCache(cacheDir, false)
    val tileFactoryExt = DefaultTileFactory(OSMTileFactoryInfo())
        .apply {
            setLocalCache(cache)
        }
    val tileFactory = tileFactoryExt // Replace with actual TileFactory implementation

    MapCompose(
        tileFactory,
        centerPoint = GeoPosition(41.740527, 44.752613),
//        waypoints = mutableStateOf(
//            listOf(
//                GeoPosition(42.50, 43.00),
//                GeoPosition(42.51, 43.01)
//            )
//        )
    )
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Compose Map Viewer") {
        MaterialTheme {
            Surface {
                PreviewMapViewer()
            }
        }
    }
}