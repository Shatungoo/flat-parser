package com.helldasy.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
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
import java.awt.Point
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.geom.Point2D


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
    zoom: Int = 5,
    visibility: MutableState<Boolean> = mutableStateOf(true),
) {
    val points = listOf(GeoPosition(lat, lng))
    MapView(points, zoom = zoom, visibility = visibility)
}

@Composable
fun MapView(
    points: List<GeoPosition>,
    zoom: Int = 5,
    visibility: MutableState<Boolean> = mutableStateOf(true),
) {
    val waypoints = points.map { DefaultWaypoint(it) }.toSet()
    val mapViewer = JXMapViewer().apply {
        tileFactory = DefaultTileFactory(OSMTileFactoryInfo())
        overlayPainter = WaypointPainter<Waypoint>().apply {
            setWaypoints(waypoints)
        }
        this.zoom = zoom
        zoomToBestFit(HashSet<GeoPosition>(points.toSet()), 0.8)
        val mia = PanMouseInputListener(this)
        addMouseListener(ClickAdapter(this, waypoints))
        addMouseListener(mia)
        addMouseMotionListener(mia)
        addMouseWheelListener(ZoomMouseWheelListenerCursor(this));
    }
    if (visibility.value)
        SwingPanel(
            modifier = Modifier.fillMaxSize(),
            factory = { mapViewer }
        )
}

class ClickAdapter(
    val mapViewer: JXMapViewer,
    val points: Set<Waypoint>,
) : MouseAdapter() {
    override fun mouseClicked(me: MouseEvent) {
        var gp_pt: Point2D? = null

        for (waypoint in points) {
            //convert to world bitmap
            gp_pt = mapViewer.tileFactory.geoToPixel(waypoint.position, mapViewer.zoom)
            //convert to screen
            val rect = mapViewer.viewportBounds
            val converted_gp_pt = Point(
                gp_pt.x.toInt() - rect.x,
                gp_pt.y.toInt() - rect.y-10
            )
            //check if near the mouse
            if (converted_gp_pt.distance(me.point) < 20) {
                println("mapViewer mouse click has been detected within 10 pixels " +
                        "of a waypoint ${waypoint}")
//                waypoint
            }
        }
    }
}
