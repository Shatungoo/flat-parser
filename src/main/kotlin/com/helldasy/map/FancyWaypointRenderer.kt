//package com.helldasy.map
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.width
//import androidx.compose.material.OutlinedButton
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.awt.SwingPanel
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.window.singleWindowApplication
//import org.jxmapviewer.JXMapViewer
//import org.jxmapviewer.OSMTileFactoryInfo
//import org.jxmapviewer.input.PanMouseInputListener
//import org.jxmapviewer.input.ZoomMouseWheelListenerCursor
//import org.jxmapviewer.viewer.*
//import java.awt.*
//import java.awt.image.*
//import javax.imageio.ImageIO
//import kotlin.math.min
//
//
//class MyWaypoint(
//    coord: GeoPosition,
//    val label: String = "",
//    val color: Color = Color.BLACK,
//) : DefaultWaypoint(coord)
//
//
//fun main() = singleWindowApplication {
//    OutlinedButton(modifier = Modifier.width(1000.dp).height(1300.dp).background(androidx.compose.ui.graphics.Color.Red),
//        onClick = {
//            println("Button clicked")
//        }
//    ) {
////        MapView()
//
//    }
//    Map(
//        points = listOf(
//            GeoPosition(50.11, 8.68),
//            GeoPosition(50.12, 8.69)
//        )
//    )
//}
//
//
//@Composable
//fun Map(
//    points: List<GeoPosition>,
//    zoom: Int = 5,
//) {
//    val waypoints = points.map { MyWaypoint(it, "point") }.toSet()
//    val mapViewer = JXMapViewer().apply {
//        tileFactory = DefaultTileFactory(OSMTileFactoryInfo())
//
//        val waypointPainter = WaypointPainter<MyWaypoint>()
//        waypointPainter.waypoints = waypoints
//        waypointPainter.setRenderer(FancyWaypointRenderer())
//
//        overlayPainter = waypointPainter
//
//        this.zoom = zoom
//        zoomToBestFit(HashSet<GeoPosition>(points.toSet()), 0.8)
//        val mia = PanMouseInputListener(this)
//        addMouseListener(ClickAdapter(this, waypoints))
//        addMouseListener(mia)
//        addMouseMotionListener(mia)
//        addMouseWheelListener(ZoomMouseWheelListenerCursor(this));
//    }
//
//    SwingPanel(
//        modifier = Modifier.fillMaxSize(),
//        factory = { mapViewer }
//    )
//}
//
//class FancyWaypointRenderer : WaypointRenderer<MyWaypoint> {
//
//    private val map: MutableMap<Color, BufferedImage?> = HashMap()
//    private var origImage: BufferedImage? = null
//    init {
//        val resource = this::class.java.classLoader.getResource("waypoint_white.png")
//
//        try {
//            origImage = ImageIO.read(resource)
//        } catch (ex: Exception) {
//            println("couldn't read waypoint_white.png")
//        }
//    }
//
//    private fun convert(loadImg: BufferedImage, newColor: Color): BufferedImage {
//        val w = loadImg.width
//        val h = loadImg.height
//        val imgOut = BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
//        val imgColor = BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
//
//        val g = imgColor.createGraphics()
//        g.color = newColor
//        g.fillRect(0, 0, w + 1, h + 1)
//        g.dispose()
//
//        val graphics = imgOut.createGraphics()
//        graphics.drawImage(loadImg, 0, 0, null)
//        graphics.composite = MultiplyComposite.Default
//        graphics.drawImage(imgColor, 0, 0, null)
//        graphics.dispose()
//
//        return imgOut
//    }
//
//    override fun paintWaypoint(g: Graphics2D, viewer: JXMapViewer, w: MyWaypoint) {
//        var g = g
//        g = g.create() as Graphics2D
//
//        if (origImage == null) return
//
//        var myImg = map[w.color]
//
//        if (myImg == null) {
//            myImg = convert(origImage!!, w.color)
//            map[w.color] = myImg
//        }
//
//        val point = viewer.tileFactory.geoToPixel(w.position, viewer.zoom)
//
//        val x = point.x.toInt()
//        val y = point.y.toInt()
//
//        g.drawImage(myImg, x - myImg.width / 2, y - myImg.height, null)
//
//        val label = w.label
//
//        //        g.setFont(font);
//        val metrics  = g.fontMetrics
//        val tw: Int = metrics.stringWidth(label)
//        val th: Int = 1 + metrics.getAscent()
//
//        //        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//        g.drawString(label, x - tw / 2, y + th - myImg.height)
//
//        g.dispose()
//    }
//}
//
//
//class MultiplyComposite private constructor() : Composite {
//    override fun createContext(
//        srcColorModel: ColorModel?,
//        dstColorModel: ColorModel?,
//        hints: RenderingHints?,
//    ): CompositeContext {
//        return object : CompositeContext {
//            override fun compose(src: Raster, dstIn: Raster, dstOut: WritableRaster) {
//                check(!(src.sampleModel.dataType != DataBuffer.TYPE_INT || dstIn.sampleModel.dataType != DataBuffer.TYPE_INT || dstOut.sampleModel.dataType != DataBuffer.TYPE_INT)) { "Source and destination must store pixels as INT." }
//
//                val width = min(src.width.toDouble(), dstIn.width.toDouble()).toInt()
//                val height = min(src.height.toDouble(), dstIn.height.toDouble()).toInt()
//
//                val srcPixel = IntArray(4)
//                val dstPixel = IntArray(4)
//                val srcPixels = IntArray(width)
//                val dstPixels = IntArray(width)
//
//                for (y in 0 until height) {
//                    src.getDataElements(0, y, width, 1, srcPixels)
//                    dstIn.getDataElements(0, y, width, 1, dstPixels)
//
//                    for (x in 0 until width) {
//                        // pixels are stored as INT_ARGB
//                        // our arrays are [R, G, B, A]
//                        var pixel = srcPixels[x]
//                        srcPixel[0] = (pixel shr 16) and 0xFF
//                        srcPixel[1] = (pixel shr 8) and 0xFF
//                        srcPixel[2] = (pixel shr 0) and 0xFF
//                        srcPixel[3] = (pixel shr 24) and 0xFF
//
//                        pixel = dstPixels[x]
//                        dstPixel[0] = (pixel shr 16) and 0xFF
//                        dstPixel[1] = (pixel shr 8) and 0xFF
//                        dstPixel[2] = (pixel shr 0) and 0xFF
//                        dstPixel[3] = (pixel shr 24) and 0xFF
//
//                        val result = intArrayOf(
//                            (srcPixel[0] * dstPixel[0]) shr 8,
//                            (srcPixel[1] * dstPixel[1]) shr 8,
//                            (srcPixel[2] * dstPixel[2]) shr 8,
//                            (srcPixel[3] * dstPixel[3]) shr 8
//                        )
//
//                        // mixes the result with the opacity
//                        dstPixels[x] =
//                            (result[3]) shl 24 or ((result[0]) shl 16
//                                    ) or ((result[1]) shl 8
//                                    ) or (result[2])
//                    }
//                    dstOut.setDataElements(0, y, width, 1, dstPixels)
//                }
//            }
//
//            override fun dispose() {}
//        }
//    }
//
//    companion object {
//        val Default: MultiplyComposite = MultiplyComposite()
//    }
//}
