package com.helldasy.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import org.jxmapviewer.viewer.GeoPosition
import org.jxmapviewer.viewer.TileFactory

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
            }
        }
    }
}
