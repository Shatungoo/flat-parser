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


@Composable
fun MapData.CenterLayer(geoPoint: GeoPosition) {
    val centerPoint = geoToPixel(geoPoint)
    val cent = center.value
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
    ) {
        val topLeft = cent - Point(size.width, size.height) / 2
        val waypointIm = PointInt(waypointImage.width / 2, waypointImage.height)
        val offset = (centerPoint - topLeft - waypointIm).toOffset()
        drawIntoCanvas { canvas -> canvas.drawImage(waypointImage, offset, Paint()) }
    }
}
