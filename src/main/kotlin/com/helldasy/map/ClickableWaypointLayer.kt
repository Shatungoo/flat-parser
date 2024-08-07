package com.helldasy.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
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


data class Rectnagle(val x: Double, val y: Double, val width: Double, val height: Double) {
    companion object {

        fun fromBottomCenter(bottomCenter: Point, width: Double, height: Double): Rectnagle {
            return Rectnagle(bottomCenter.x - width / 2, bottomCenter.y - height, width, height)
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

    val bottomCenter = Point((x + width / 2).toFloat() , (y + height).toFloat())

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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MapData.ClickableWaypointLayer(selectablePoints: List<SelectablePoint>, onClick: (data: List<*>) -> Unit = {}) {
    val cent = center.value
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
            .onPointerEvent(PointerEventType.Press) {event ->
                val click = event.changes.first().position
                val list = selectablePoints.mapNotNull { data ->
                    val pointLocal = tileFactory.geoToPixel(data.geoPoistion, zoomLevel).toPoint() - Point(
                        cent.x - size.width / 2,
                        cent.y - size.height / 2
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
            .pointerInput(Unit) {
                detectTransformGestures { c, pan, _, _ -> moveCenter(pan) }
            }
            .onPointerEvent(PointerEventType.Scroll) {
                zoomIn(it.changes.first().scrollDelta.y)
            }

    ) {
        val topLeft = cent - Point(size.width, size.height) / 2
        drawIntoCanvas {canvas ->
            selectablePoints.forEach { waypoint ->
                val point = geoToPixel(waypoint.geoPoistion)
                val offset = (point - topLeft - waypoint.imageBox.bottomCenter).toOffset()
                canvas.drawImage(waypoint.image.value, offset, Paint())
            }
        }
    }
}
