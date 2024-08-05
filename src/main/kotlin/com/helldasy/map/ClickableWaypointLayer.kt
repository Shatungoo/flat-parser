package com.helldasy.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import org.jxmapviewer.viewer.GeoPosition
import org.jxmapviewer.viewer.TileFactory

data class ClickablePoint<T>(val coord: GeoPosition, val data: T) {}


class ClickableWaypointLayer(val points: List<ClickablePoint<*>>, val onClick: (data: Any?) -> Unit = {}) : ILayer {
    val selectedWaypoint = mutableStateOf<GeoPosition?>(null)

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    override fun Layer(
        tileFactory: TileFactory,
        centerP: MutableState<Point>,
        zoom: MutableState<Int>,
    ) {
        val center = centerP.value
        val zoomLevel = zoom.value
        val waypointImageRect = Rectnagle(0.0, 0.0, waypointImage.width.toDouble(), waypointImage.height.toDouble())

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
//                .onPointerEvent(PointerEventType.Press) {
//                    val offset = it.changes.first().position
//                    val topLeft = Point(center.x - size.width / 2, center.y - size.height / 2)
//                    points.forEach { data ->
//                        val waypoint = data.coord
//                        val point = tileFactory.geoToPixel(waypoint, zoomLevel).toPoint()
//                        val imagePoint = (point - topLeft - waypointImageRect.bottomCenter).toOffset()
//                        if (waypointImageRect.contains(point - topLeft)) {
//                            selectedWaypoint.value = waypoint
//                            println(waypoint)
//                            onClick(data.data)
//                        }
//                    }
//                },
        ) {
//            val topLeft = Point(center.x - size.width / 2, center.y - size.height / 2)
//            drawIntoCanvas { canvas ->
//                points.forEach { point ->
//                    val waypoint = point.coord
//                    val waypointPx = tileFactory.geoToPixel(waypoint, zoomLevel).toPoint()
//
//                    val imagePoint = (waypointPx - topLeft - waypointImageRect.bottomCenter).toOffset()
//                    if (imagePoint.x >= -100
//                        && imagePoint.x <= size.width+100
//                        && imagePoint.y >= -100
//                        && imagePoint.y <= size.height+100
//                        ) {
//                            canvas.drawImage(waypointImage, imagePoint, Paint())
//                    }
//                }
//            }
            val topLeft = Point(center.x - size.width / 2, center.y - size.height / 2)
            drawIntoCanvas { canvas ->
                points.forEach { waypoint ->
                    val point = tileFactory.geoToPixel(waypoint.coord, zoomLevel).toPoint()
                    val waypointIm = PointInt(waypointImage.width / 2, waypointImage.height)
                    val offset = (point - topLeft - waypointIm).toOffset()
                    canvas.drawImage(waypointImage, offset, Paint())
                }
            }
        }
    }
}
