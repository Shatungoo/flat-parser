package com.helldasy.map

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import org.jxmapviewer.viewer.GeoPosition
import org.jxmapviewer.viewer.TileFactory


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MapData.MoveableLayer() {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
            .pointerInput(Unit) {
                detectTransformGestures { c, pan, _, _ -> moveCenter(pan) }
            }
            .onPointerEvent(PointerEventType.Scroll) {
                zoomIn(it.changes.first().scrollDelta.y)
            }
    ) {}
}
