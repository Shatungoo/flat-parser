package com.helldasy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.singleWindowApplication
import org.jxmapviewer.JXMapViewer
import org.jxmapviewer.OSMTileFactoryInfo
import org.jxmapviewer.input.PanMouseInputListener
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor
import org.jxmapviewer.viewer.*
import javax.swing.event.MouseInputListener


fun main() = singleWindowApplication {
    OutlinedButton(modifier = Modifier.width(1000.dp).height(1300.dp).background(Color.Red),
        onClick = {
            println("Button clicked")
        }
    ) {
//        MapView()

    }
    MapView(
        points = listOf(
            GeoPosition(50.11, 8.68),
            GeoPosition(50.12, 8.69)
        )
    )
}


@Composable
fun MapView(
    lat: Double = 50.11,
    lng: Double = 8.68,
) {
    val points = listOf(GeoPosition(lat, lng))
    MapView(points)
}

@Composable
fun MapView(
    points: List<GeoPosition>,
) {
    val waypoints = points.map { DefaultWaypoint(it) }.toSet()
    val mapViewer = JXMapViewer().apply {
        tileFactory = DefaultTileFactory(OSMTileFactoryInfo())
        overlayPainter = WaypointPainter<Waypoint>().apply {
            setWaypoints(waypoints)
        }
        zoomToBestFit(HashSet<GeoPosition>(points.toSet()), 0.8)

        val mia: MouseInputListener = PanMouseInputListener(this)
        addMouseListener(mia)
        addMouseMotionListener(mia)
        addMouseWheelListener(ZoomMouseWheelListenerCursor(this));
    }

    SwingPanel(
        modifier = Modifier.fillMaxSize(),
        factory = { mapViewer }
    )


    SwingPanel(
        modifier = Modifier.fillMaxSize(),
        factory = { mapViewer }
    )
}
