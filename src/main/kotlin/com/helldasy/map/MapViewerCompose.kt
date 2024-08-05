@file:OptIn(ExperimentalComposeUiApi::class)

package com.helldasy.map

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
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
import org.jxmapviewer.viewer.Tile
import org.jxmapviewer.viewer.TileFactory
import java.awt.geom.AffineTransform
import java.awt.geom.Point2D
import java.awt.image.AffineTransformOp
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



val loadingImage = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).toComposeImageBitmap()

data class MapData(
    val tileFactory: TileFactory,
    val center: GeoPosition,
    val zoom: Int,
) {
    fun localToGlobal(x: Double, y: Double): Point {
        val a = tileFactory.geoToPixel(center, zoom)
        return Point(a.x + x, a.y + y)
    }

    fun globalToLocal(x: Double, y: Double): Point {
        val a = tileFactory.geoToPixel(center, zoom)
        return Point(x - a.x, y - a.y)
    }

    fun localToScreen(x: Double, y: Double): Point {
        val a = tileFactory.geoToPixel(center, zoom)
        return Point(a.x + x, a.y + y)
    }

    fun screenToLocal(x: Double, y: Double): Point {
        val a = tileFactory.geoToPixel(center, zoom)
        return Point(x - a.x, y - a.y)
    }

    fun screenToGlobal(x: Double, y: Double): Point {
        val a = tileFactory.geoToPixel(center, zoom)
        return Point(a.x + x, a.y + y)
    }

    fun globalToScreen(x: Double, y: Double): Point {
        val a = tileFactory.geoToPixel(center, zoom)
        return Point(x - a.x, y - a.y)
    }
}

@Composable
fun MapCompose(
    tileFactory: TileFactory,
    centerPoint: GeoPosition = GeoPosition(42.50, 43.00),
    zoom: Int = 8,
    layer: ILayer? = WaypointLayer(listOf(centerPoint)),
) {
    val zoomLevel = mutableStateOf(zoom)
    val center = mutableStateOf(tileFactory.geoToPixel(centerPoint, zoomLevel.value).toPoint())
    Box(modifier = Modifier.fillMaxSize()) {

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
                .pointerInput(Unit) {
                    detectTransformGestures { c, pan, _, _ ->
                        center.value -= pan
                    }
                }
                .onPointerEvent(PointerEventType.Press){
                    layer?.onEvent(it, tileFactory, center.value, zoomLevel.value, size.toSize())
                }
                .onPointerEvent(PointerEventType.Scroll) {
                    val zoomL = it.changes.first().scrollDelta.y
                    val oldMapSize = tileFactory.getMapSize(zoomLevel.value)
                    zoomLevel.value = (zoomLevel.value + zoomL).toInt().coerceIn(
                        tileFactory.info.minimumZoomLevel,
                        tileFactory.info.maximumZoomLevel
                    )
                    val mapSize = tileFactory.getMapSize(zoomLevel.value)
                    val oldCenter = center.value
                    center.value = Point(
                        oldCenter.x * (mapSize.getWidth() / oldMapSize.getWidth()),
                        oldCenter.y * (mapSize.getHeight() / oldMapSize.getHeight())
                    )
                },
        ) {
            val tileSize = tileFactory.getTileSize(zoomLevel.value)
            val topLeft = Point(center.value.x - size.width / 2, center.value.y - size.height / 2)

            val startTileX = floor(topLeft.x / tileSize).toInt() // номер тайла
            val startTileY = floor(topLeft.y / tileSize).toInt()

            val numWide = (this.size.width / tileSize).toInt() + 2 // количество тайлов по ширине
            val numHigh = (this.size.height / tileSize).toInt() + 2
            val paint = Paint()
            drawIntoCanvas { canvas ->
                for (itpx in startTileX.rangeTo(startTileX + numWide))
                    for (itpy in startTileY..(startTileY + numHigh)) {
//                    CoroutineScope(Dispatchers.Default).launch {
                        val ox = itpx * tileSize - topLeft.x // координаты тайла на экране
                        val oy = itpy * tileSize - topLeft.y

                        val image = getTile(tileFactory, itpx, itpy, zoomLevel.value)
                        canvas.drawImage(
                            image.value,
                            Offset(ox.toFloat(), oy.toFloat()),
                            paint
                        )
//                    }
                    }
            }
        }

        layer?.Layer(
            tileFactory,
            center,
            zoomLevel,
        )
    }
}

private val localCache = mutableMapOf<String, ImageBitmap>()

private fun getTile(
    tileFactory: TileFactory,
    itpx: Int,
    itpy: Int,
    zoomLevel: Int,
//    image: MutableState<ImageBitmap>,
): MutableState<ImageBitmap> {
    val key = "$itpx-$itpy-$zoomLevel"
    if (localCache.containsKey(key)) {
        return mutableStateOf(localCache[key]!!)
    }
    tileFactory.getTile(itpx, itpy, zoomLevel)?.let {
        var tile = it
        if (tile.isLoaded) {
            return mutableStateOf(tile.image.toComposeImageBitmap())
        }
        val image = mutableStateOf(loadingImage)
        CoroutineScope(Dispatchers.Default).launch {
            while (!tile.isLoaded or tile.loadingFailed()) {
                delay(40)
                tile = tileFactory.getTile(itpx, itpy, zoomLevel)
            }
            localCache[key] = tile.image.toComposeImageBitmap()
            image.value = tile.image.toComposeImageBitmap()
        }

        val superTile: Tile? = if (zoomLevel < tileFactory.info.maximumZoomLevel) {
            tileFactory.getTile(itpx / 2, itpy / 2, zoomLevel + 1)
        } else null
        val zoomOld = zoomLevel
        if (superTile != null && superTile.isLoaded) {
//        if (localCache.containsKey("${itpx / 2}-${itpy / 2}-${zoomLevel + 1}")) {
            val size = tileFactory.getTileSize(zoomLevel)
            val imageX: Int = itpx % 2 * size / 2
            val imageY: Int = itpy % 2 * size / 2

            image.value = superTile.image.getSubimage(imageX, imageY, size / 2, size / 2)
                .scale(2.0)
                .toComposeImageBitmap()
            return image
        }
        return image
    }
    return mutableStateOf(loadingImage)
}

private fun BufferedImage.scale(scale: Double): BufferedImage {
    val before = this
    val w = before.width
    val h = before.height
    // Create a new image of the proper size
    val w2 = (w * scale).toInt()
    val h2 = (h * scale).toInt()
    val after = BufferedImage(w2, h2, BufferedImage.TYPE_INT_ARGB)
    val scaleInstance = AffineTransform.getScaleInstance(scale, scale)
    val scaleOp = AffineTransformOp(scaleInstance, AffineTransformOp.TYPE_BILINEAR)

    scaleOp.filter(before, after)
    return after
}


interface ILayer {
    @Composable
    fun Layer(
        tileFactory: TileFactory,
        centerP: MutableState<Point>,
        zoom: MutableState<Int>,
    )

    fun onEvent(event: PointerEvent, tileFactory: TileFactory, center: Point, zoomLevel: Int, size: Size){}
}

fun Point2D.toPoint(): Point {
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

    Box(
        modifier = Modifier.size(800.dp).border(1.dp, Color.Black)
            .wrapContentSize(align = Alignment.TopCenter, unbounded = false)
    ) {
        Column {
            MapCompose(
                tileFactory,
                centerPoint = GeoPosition(41.740527, 44.752613),
                layer = CenterLayer(GeoPosition(41.740527, 44.752613))
//                WaypointLayer(
//                    listOf(
//                        GeoPosition(41.740527, 44.752613),
//                        GeoPosition(41.697537, 44.820813),
//                    )
//                )
            )
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
