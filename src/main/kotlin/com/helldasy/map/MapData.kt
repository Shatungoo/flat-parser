@file:OptIn(ExperimentalComposeUiApi::class)

package com.helldasy.map

import androidx.compose.foundation.Canvas
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

    operator fun rangeTo(other: PointInt): List<PointInt> {
        val list = mutableListOf<PointInt>()
        for (i in x..other.x)
            for (j in y..other.y)
                list.add(PointInt(i, j))
        return list
    }

    operator fun times(tileSize: Int): PointInt = PointInt(x * tileSize, y * tileSize)
    operator fun minus(other: Point): Point = Point(x - other.x, y - other.y)
    operator fun plus(i: Int): PointInt = PointInt(x + i, y + i)
}

data class Point(val x: Float, val y: Float) {
    operator fun minus(other: Point) = Point(x - other.x, y - other.y)
    operator fun plus(other: Point) = Point(x + other.x, y + other.y)

    operator fun minus(other: Offset) = Point(x - other.x, y - other.y)
    operator fun plus(other: Offset) = Point(x + other.x, y + other.y)

    operator fun times(other: Double) = Point(x * other.toFloat(), y * other.toFloat())
    operator fun div(other: Double) = Point(x / other.toFloat(), y / other.toFloat())

    operator fun div(other: Point) = Point(x / other.x, y / other.y)
    operator fun div(other: Int) = Point(x / other, y / other)

    fun toOffset() = Offset(x.toFloat(), y.toFloat())
    operator fun plus(other: PointInt) = Point(x + other.x, y + other.y)
    operator fun minus(other: PointInt) = Point(x - other.x, y - other.y)

}

val loadingImage = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).toComposeImageBitmap()

data class MapData(
    val tileFactory: TileFactory,
    val center: MutableState<Point>,
    val zoom: MutableState<Int>,
) {
    companion object {
        fun create(tileFactory: TileFactory, center: GeoPosition, zoom: Int): MapData {
            val cent = tileFactory.geoToPixel(center, zoom).toPoint()
            return MapData(tileFactory, mutableStateOf(cent), mutableStateOf(zoom))
        }

        fun getCenter(geoPositions: List<GeoPosition>): GeoPosition {
            val x = geoPositions.map { it.latitude }.average()
            val y = geoPositions.map { it.longitude }.average()
            return GeoPosition(x, y)
        }
    }

    val zoomLevel: Int
        get() = zoom.value
    val centerPoint: Point
        get() = center.value

    val tileSize: Int
        get() = tileFactory.getTileSize(zoomLevel)

    fun geoToPixel(geo: GeoPosition): Point = tileFactory.geoToPixel(geo, zoomLevel).toPoint()

    fun zoomIn(zoomDiff: Float) {
        val oldMapSize = tileFactory.getMapSize(zoomLevel)
        zoom.value = (zoomLevel + zoomDiff).toInt().coerceIn(
            tileFactory.info.minimumZoomLevel,
            tileFactory.info.maximumZoomLevel
        )
        val mapSize = tileFactory.getMapSize(zoomLevel)
        val oldCenter = center.value
        center.value = Point(
            oldCenter.x * (mapSize.getWidth() / oldMapSize.getWidth()).toFloat(),
            oldCenter.y * (mapSize.getHeight() / oldMapSize.getHeight()).toFloat()
        )
    }

    fun moveCenter(pan: Offset) {
        center.value -= pan
    }

    @Composable
    fun Map(
        content: @Composable (mapData: MapData) -> Unit = {},
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .clipToBounds()
            ) {
                val topLeft = centerPoint - Point(size.width, size.height) / 2
                val startTile = PointInt(
                    floor(topLeft.x / tileSize).toInt(),
                    floor(topLeft.y / tileSize).toInt()
                )
                val endTile = startTile +
                        PointInt((this.size.width / tileSize).toInt(), (this.size.height / tileSize).toInt()) + 2
                val paint = Paint()
                drawIntoCanvas { canvas ->
                    for (itp in startTile..endTile) {
                        val o = itp * tileSize - topLeft
                        val image = getTile(itp.x, itp.y)
                        canvas.drawImage(
                            image.value,
                            o.toOffset(),
                            paint
                        )
                    }
                }
            }
            content(this@MapData)
        }
    }

    private val localCache = mutableMapOf<String, ImageBitmap>()

    private fun getTile(
        itpx: Int,
        itpy: Int,
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
    return Point(x.toFloat(), y.toFloat())
}
