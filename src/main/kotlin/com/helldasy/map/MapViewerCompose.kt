package com.helldasy.map

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.helldasy.getTemporalDirectory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jxmapviewer.OSMTileFactoryInfo
import org.jxmapviewer.cache.FileBasedLocalCache
import org.jxmapviewer.viewer.DefaultTileFactory
import org.jxmapviewer.viewer.GeoPosition
import org.jxmapviewer.viewer.TileFactory
import java.awt.geom.Point2D
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import kotlin.math.floor

data class PointInt(val x: Int, val y: Int) {
    operator fun minus(other: PointInt) = PointInt(x - other.x, y - other.y)
    operator fun plus(other: PointInt) = PointInt(x + other.x, y + other.y)

    operator fun div(other: PointInt) = PointInt(x / other.x, y / other.y)
    operator fun div(other: Int) = PointInt(x / other, y / other)

    fun toOffset() = Offset(x.toFloat(), y.toFloat())
}

data class Point(val x: Double, val y: Double) {
    operator fun minus(other: Point) = Point(x - other.x, y - other.y)
    operator fun plus(other: Point) = Point(x + other.x, y + other.y)

    operator fun minus(other: Offset) = Point(x - other.x, y - other.y)
    operator fun plus(other: Offset) = Point(x + other.x, y + other.y)

    operator fun times(other: Double) = Point(x * other, y * other)
    operator fun div(other: Double) = Point(x / other, y / other)

    operator fun div(other: Point) = Point(x / other.x, y / other.y)
    operator fun div(other: Int) = Point(x / other, y / other)

    fun toOffset() = Offset(x.toFloat(), y.toFloat())
    operator fun plus(other: PointInt) = Point(x + other.x, y + other.y)
    operator fun minus(other: PointInt) = Point(x - other.x, y - other.y)

}


val waypointImage =
    ImageIO
        .read(
            object {}.javaClass
                .getResource("/waypoint_white.png")
        )
        .toComposeImageBitmap()

val loadingImage = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).toComposeImageBitmap()

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MapCompose(
    tileFactory: TileFactory,
    centerPoint: GeoPosition = GeoPosition(42.50, 43.00),
    zoom: Int = 8,
    layer: ILayer? = CenterLayer(centerPoint),
    lay: () -> Unit = {}
) {
    var zoomLevel by remember { mutableStateOf(zoom) }
    val a = tileFactory.geoToPixel(centerPoint, zoomLevel)
    var center by remember { mutableStateOf(Point(a.x, a.y)) }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
            .pointerInput(Unit) {
                detectTransformGestures { c, pan, _, _ ->
//                    center -= pan
                    println(c)
                    center -= pan
                }
            }
            .onPointerEvent(PointerEventType.Scroll) {
                val zoomL = it.changes.first().scrollDelta.y
                val oldMapSize = tileFactory.getMapSize(zoomLevel)
                zoomLevel = (zoomLevel + zoomL).toInt().coerceIn(
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
                for (itpy in startTileY..(startTileY + numHigh)) {
                    val ox = itpx * tileSize - topLeft.x // координаты тайла на экране
                    val oy = itpy * tileSize - topLeft.y

                    val image = mutableStateOf(loadingImage)
                    tileFactory.getTile(itpx, itpy, zoomLevel)?.let {
                        var tile = it
                        if (tile.isLoaded) {
                            image.value = tile.image.toComposeImageBitmap()
                        } else CoroutineScope(Dispatchers.Default).launch {
                            while (!tile.isLoaded or tile.loadingFailed()) {
                                delay(40)
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

        }
    }

    layer?.Layer(
        tileFactory,
        mutableStateOf(center),
        mutableStateOf(zoomLevel),
    )
}

class CenterLayer(val point1: GeoPosition) : ILayer {
    @Composable
    override fun Layer(
        tileFactory: TileFactory,
        centerP: MutableState<Point>,
        zoom: MutableState<Int>,

        ) {
        val center = centerP.value
        val zoomLevel = zoom.value
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
        ) {
            val topLeft = Point(center.x - size.width / 2, center.y - size.height / 2)
            drawIntoCanvas { canvas ->
                val point = tileFactory.geoToPixel(point1, zoomLevel).toPoint()
                val waypointIm = PointInt(waypointImage.width / 2, waypointImage.height)
                val offset = (point - topLeft - waypointIm).toOffset()
                canvas.drawImage(waypointImage, offset, Paint())
//            canvas.drawCircle(offset, 5f, Paint().apply { color = Color.Red })
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
}

class WaypointLayer(val points: List<GeoPosition>) : ILayer {
    @Composable
    override fun Layer(
        tileFactory: TileFactory,
        centerP: MutableState<Point>,
        zoom: MutableState<Int>,

        ) {
        val center = centerP.value
        val zoomLevel = zoom.value
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
        ) {
            val topLeft = Point(center.x - size.width / 2, center.y - size.height / 2)
            drawIntoCanvas { canvas ->
                points.forEach { waypoint ->
                    val point = tileFactory.geoToPixel(waypoint, zoomLevel).toPoint()
                    val waypointIm = PointInt(waypointImage.width / 2, waypointImage.height)
                    val offset = (point - topLeft - waypointIm).toOffset()
                    canvas.drawImage(waypointImage, offset, Paint())
                }
            }
        }
    }
}

interface ILayer {
    @Composable
    fun Layer(
        tileFactory: TileFactory, centerP: MutableState<Point>,
        zoom: MutableState<Int>,
    )
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
    Box(modifier = Modifier.size(600.dp).background(Color.Red)) {

        Box(
            modifier = Modifier.size(150.dp).border(1.dp, Color.Black)
                .wrapContentSize(align = Alignment.TopCenter, unbounded = false)
        ) {
            Column {
                MapCompose(
                    tileFactory,
                    centerPoint = GeoPosition(41.740527, 44.752613),
                    layer = CenterLayer(GeoPosition(41.740527, 44.752613))
                )
            }
        }
    }
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