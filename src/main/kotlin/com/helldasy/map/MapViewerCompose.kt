@file:OptIn(ExperimentalComposeUiApi::class)

package com.helldasy.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.toSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jxmapviewer.viewer.GeoPosition
import org.jxmapviewer.viewer.Tile
import org.jxmapviewer.viewer.TileFactory
import java.awt.geom.AffineTransform
import java.awt.geom.Point2D
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage
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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MapCompose(
    tileFactory: TileFactory,
    centerPoint: GeoPosition = GeoPosition(42.50, 43.00),
    zoom: Int = 8,
    layer: ILayer? = CenterLayer(centerPoint),
    a: @Composable () -> Unit = {},
) {
    val zoomLevel1 = mutableStateOf (zoom)
    var zoomLevel by zoomLevel1
    val center = mutableStateOf(tileFactory.geoToPixel(centerPoint, zoom).toPoint())
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
                    layer?.onEvent(it, tileFactory, center.value, zoomLevel, size.toSize())
                }
                .onPointerEvent(PointerEventType.Scroll) {
                    val zoomL = it.changes.first().scrollDelta.y
                    val oldMapSize = tileFactory.getMapSize(zoomLevel)
                    zoomLevel = (zoomLevel + zoomL).toInt().coerceIn(
                        tileFactory.info.minimumZoomLevel,
                        tileFactory.info.maximumZoomLevel
                    )
                    val mapSize = tileFactory.getMapSize(zoomLevel)
                    val oldCenter = center.value
                    center.value = Point(
                        oldCenter.x * (mapSize.getWidth() / oldMapSize.getWidth()),
                        oldCenter.y * (mapSize.getHeight() / oldMapSize.getHeight())
                    )
                },
        ) {
            val tileSize = tileFactory.getTileSize(zoomLevel)
            val topLeft = Point(center.value.x - size.width / 2, center.value.y - size.height / 2)

            val startTileX = floor(topLeft.x / tileSize).toInt() // номер тайла
            val startTileY = floor(topLeft.y / tileSize).toInt()

            val numWide = (this.size.width / tileSize).toInt() + 2 // количество тайлов по ширине
            val numHigh = (this.size.height / tileSize).toInt() + 2
            val paint = Paint()
            drawIntoCanvas { canvas ->
                for (itpx in startTileX.rangeTo(startTileX + numWide))
                    for (itpy in startTileY..(startTileY + numHigh)) {
                        val ox = itpx * tileSize - topLeft.x // координаты тайла на экране
                        val oy = itpy * tileSize - topLeft.y

                        val image = getTile(tileFactory, itpx, itpy, zoomLevel)
                        canvas.drawImage(
                            image.value,
                            Offset(ox.toFloat(), oy.toFloat()),
                            paint
                        )
                    }
            }
        }

        layer?.Layer(
            tileFactory,
            center,
            zoomLevel1,
        )
        a()
    }
}

private val localCache = mutableMapOf<String, ImageBitmap>()

private fun getTile(
    tileFactory: TileFactory,
    itpx: Int,
    itpy: Int,
    zoomLevel: Int,
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
        if (superTile != null && superTile.isLoaded) {
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

fun Point2D.toPoint(): Point {
    return Point(x, y)
}
