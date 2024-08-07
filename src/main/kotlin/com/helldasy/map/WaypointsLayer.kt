package com.helldasy.map

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import org.jxmapviewer.viewer.GeoPosition


@Composable
fun MapData.WaypointsLayer(geoPoint: GeoPosition) {
    WaypointsLayer(listOf(geoPoint))
}

@Composable
fun MapData.WaypointsLayer(geoPoints: List<GeoPosition>) {
    val map = this
    val cent = center.value
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
    ) {
        geoPoints.forEach { geoPoint ->
            val pointPx = map.geoToPixel(geoPoint)
            val topLeft = cent - Point(size.width, size.height) / 2
            val waypointIm = PointInt(waypointImage.width / 2, waypointImage.height)
            val offset = (pointPx - topLeft - waypointIm).toOffset()
            drawIntoCanvas { canvas -> canvas.drawImage(waypointImage, offset, Paint()) }
        }

    }
}
