package com.helldasy.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposePanel
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.singleWindowApplication
import com.helldasy.Response
import com.helldasy.getTemporalDirectory
import org.jxmapviewer.JXMapViewer
import org.jxmapviewer.OSMTileFactoryInfo
import org.jxmapviewer.cache.FileBasedLocalCache
import org.jxmapviewer.input.PanMouseInputListener
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor
import org.jxmapviewer.viewer.*
import java.awt.Point
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent


fun main() = singleWindowApplication {
    OutlinedButton(modifier = Modifier.width(1000.dp).height(1300.dp).background(Color.Red),
        onClick = {
            println("Button clicked")
        }
    ) {

    }

}

val cacheDir = getTemporalDirectory(".osm")
val cache = FileBasedLocalCache(cacheDir, false)
val tileFactory = DefaultTileFactory(OSMTileFactoryInfo())
    .apply {
        setLocalCache(cache)
    }

@Composable
fun MapComposeSmall(
    lat: Double = 50.11,
    lng: Double = 8.68,
    zoom: Int = 5,
){
    MapCompose(
        centerPoint = GeoPosition(lat, lng),
        tileFactory = tileFactory,
        zoom = zoom,
    )
}

@Composable
fun MapComposeBig(
    zoom: Int = 5,
    points: List<ClickablePoint<*>>,
    onClick: (Set<ClickableWaypoint>) -> Unit = {},
){
    MapCompose(
        centerPoint = points.first().coord,
        tileFactory = tileFactory,
        zoom = zoom,
//        layer = ClickableWaypointLayer(points)
        layer = CenterLayer(points.map { it.coord }.first())
    )
}

@Composable
fun MapSwing(
    lat: Double = 50.11,
    lng: Double = 8.68,
    zoom: Int = 5,
    visibility: MutableState<Boolean> = mutableStateOf(true),
) {
    val position = GeoPosition(lat, lng)
    val waypoints = setOf(DefaultWaypoint(position))
    val mapViewer = JXMapViewer().apply {
        tileFactory = DefaultTileFactory(OSMTileFactoryInfo())
        overlayPainter = WaypointPainter<Waypoint>().apply {
            setWaypoints(waypoints)
        }
        this.zoom = zoom
        zoomToBestFit(setOf(position), 0.8)
        val mia = PanMouseInputListener(this)
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


@Composable
fun MapSwing(
    points: List<Response.Flat>,
    zoom: Int = 5,
    modifier: Modifier = Modifier.fillMaxSize(),
    visibility: MutableState<Boolean> = mutableStateOf(true),
    onClick: (List<Response.Flat>) -> Unit = {},
    content: @Composable () -> Unit = {},
) {
    val waypoints = points.map {
        ClickableWaypoint(
            GeoPosition(it.lat!!, it.lng!!),
            data = it
        )
    }.toSet()
    val mapViewer = JXMapViewer().apply {
        this.tileFactory = tileFactory
        overlayPainter = WaypointPainter<Waypoint>().apply {
            setWaypoints(waypoints)
        }
        this.zoom = zoom
        zoomToBestFit(points.mapNotNull {
            if (it.lat != null && it.lng != null)
                GeoPosition(it.lat, it.lng) else null
        }.toSet(), 0.8)
        val mia = PanMouseInputListener(this)
        addMouseListener(ClickAdapter(this, waypoints, onClick = { it ->
            onClick(it.map { it.data })
        }))
        addMouseListener(mia)
        addMouseMotionListener(mia)
        addMouseWheelListener(ZoomMouseWheelListenerCursor(this));
        add(ComposePanel().apply { setContent { content() }})
    }
    SwingPanel(
        modifier = modifier,
        factory = { mapViewer }
    )
}

class ClickableWaypoint(
    val coord: GeoPosition,
    val color: java.awt.Color = java.awt.Color.BLACK,
    val data: Response.Flat
) : DefaultWaypoint(coord)

class ClickAdapter(
    val mapViewer: JXMapViewer,
    val points: Set<ClickableWaypoint>,
    val onClick: (Set<ClickableWaypoint>) -> Unit = {}
) : MouseAdapter() {
    override fun mouseClicked(me: MouseEvent) {
        val clicked: MutableSet<ClickableWaypoint> = mutableSetOf()
        for (waypoint in points) {
            //convert to world bitmap
            val point = mapViewer.tileFactory.geoToPixel(waypoint.position, mapViewer.zoom)
            //convert to screen
            val rect = mapViewer.viewportBounds
            val converted_gp_pt = Point(
                point.x.toInt() - rect.x,
                point.y.toInt() - rect.y - 10
            )
            //check if near the mouse
            if (converted_gp_pt.distance(me.point) < 20) {
                clicked.add(waypoint)
            }
        }
        if (clicked.isNotEmpty()) {
            onClick(clicked)
        }
    }
}
