package com.helldasy.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventType
import com.helldasy.map.Rectnagle.Companion.fromBottomCenter
import org.jxmapviewer.viewer.GeoPosition
import org.jxmapviewer.viewer.TileFactory
import javax.imageio.ImageIO

val waypointImage =
    ImageIO
        .read(
            object {}.javaClass
                .getResource("/standard_waypoint.png")
        )
        .toComposeImageBitmap()

val selectedWaypoint =
    ImageIO
        .read(
            object {}.javaClass
                .getResource("/waypoint_red.png")
        )
        .toComposeImageBitmap()

data class ClickablePoint<T>(val coord: GeoPosition, val data: T) {}

data class Rectnagle(val x: Double, val y: Double, val width: Double, val height: Double) {

    companion object {
        fun fromTopLeft(x: Double, y: Double, width: Double, height: Double): Rectnagle {
            return Rectnagle(x, y - height, width.toDouble(), height.toDouble())
        }

        fun fromCenter(center: Point, width: Double, height: Double): Rectnagle {
            return Rectnagle(center.x - width / 2, center.y - height / 2, width, height)
        }

        fun fromCenter(center: Point, width: Float, height: Float): Rectnagle {
            return Rectnagle(center.x - width / 2, center.y - height / 2, width.toDouble(), height.toDouble())
        }

        fun fromBottomCenter(bottomCenter: Point, width: Double, height: Double): Rectnagle {
            return Rectnagle(bottomCenter.x - width / 2, bottomCenter.y - height, width, height)
        }

        fun fromTopLeft(topLeft: Point, width: Double, height: Double): Rectnagle {
            return Rectnagle(topLeft.x, topLeft.y, width, height)
        }

        fun fromSize(toDouble: Double, toDouble1: Double): Rectnagle {
            return Rectnagle(0.0, 0.0, toDouble, toDouble1)
        }
    }

    fun contains(point: Point): Boolean {
        return point.x >= x && point.x <= x + width && point.y >= y && point.y <= y + height
    }

    fun contains(point: Offset): Boolean {
        return point.x >= x && point.x <= x + width && point.y >= y && point.y <= y + height
    }

    fun contains(that: Rectnagle): Boolean {
        return this.x <= that.x && this.y <= that.y && this.x + this.width >= that.x + that.width && this.y + this.height >= that.y + that.height
    }

    val center = Point(x + width / 2, y + height / 2)
    val bottomCenter = Point(x + width / 2, y + height)
    val topLeft = Point(x, y)

    val topRight = Point(x + width, y)
    val bottomLeft = Point(x, y + height)
}


data class SelectablePoint(
    val geoPoistion: GeoPosition,
    val data: Any?,
    val image: MutableState<ImageBitmap> = mutableStateOf(waypointImage),
    val selected: MutableState<Boolean> = mutableStateOf(false),
) {
    fun select() {
        image.value = selectedWaypoint
        selected.value = true
    }

    fun deselect() {
        image.value = waypointImage
        selected.value = false
    }

    val imageBox: Rectnagle
        get() = Rectnagle.fromSize(waypointImage.width.toDouble(), waypointImage.height.toDouble())
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

class ClickableWaypointLayer(val selectablePoints: List<SelectablePoint>, val onClick: (data: List<*>) -> Unit = {}) :
    ILayer {

    override fun onEvent(event: PointerEvent, tileFactory: TileFactory, center: Point, zoomLevel: Int, size: Size) {
        if (event.type == PointerEventType.Press) {
            val click = event.changes.first().position
            val list = selectablePoints.mapNotNull { data ->
                val pointLocal = tileFactory.geoToPixel(data.geoPoistion, zoomLevel).toPoint() - Point(
                    center.x - size.width / 2,
                    center.y - size.height / 2
                )
                val waypointIcon =
                    fromBottomCenter(pointLocal, waypointImage.width.toDouble(), waypointImage.height.toDouble())
                if (waypointIcon.contains(click)) {
                    data.select()
                    data.data!!
                } else if (data.selected.value) {
                    data.deselect()
                    null
                } else null
            }
            if (list.isNotEmpty()) onClick(list)
        }
    }

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
                selectablePoints.forEach { waypoint ->

                    val point = tileFactory.geoToPixel(waypoint.geoPoistion, zoomLevel).toPoint()
                    val offset = (point - topLeft - waypoint.imageBox.bottomCenter).toOffset()
                    canvas.drawImage(waypoint.image.value, offset, Paint())
                }
            }
        }
    }
}
