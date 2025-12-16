
package com.volodymyr.easynotes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun DrawingScreen() {
    val paths = remember { mutableStateListOf<Path>() }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(true) {
                detectDragGestures { change, dragAmount ->
                    change.consume()

                    val path = Path()
                    path.moveTo(change.position.x - dragAmount.x, change.position.y - dragAmount.y)
                    path.lineTo(change.position.x, change.position.y)

                    paths.add(path)
                }
            }
    ) {
        paths.forEach { path ->
            drawPath(
                path = path,
                color = androidx.compose.ui.graphics.Color.Black,
                style = Stroke(width = 5f)
            )
        }
    }
}
