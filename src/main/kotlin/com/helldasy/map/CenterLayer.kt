package com.helldasy.map

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import org.jxmapviewer.viewer.GeoPosition
import org.jxmapviewer.viewer.TileFactory

class CenterLayer(val point: GeoPosition) : ILayer {
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
            val point = tileFactory.geoToPixel(point, zoomLevel).toPoint()
            val waypointIm = PointInt(waypointImage.width / 2, waypointImage.height)
            val offset = (point - topLeft - waypointIm).toOffset()
            println("offset: $offset")
            drawIntoCanvas { canvas ->
                canvas.drawImage(waypointImage, offset, Paint())
            }

        }
    }
}
