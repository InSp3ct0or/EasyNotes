package com.volodymyr.easynotes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun DrawingScreen() {
    val drawingPaths = remember { mutableStateListOf<DrawingPath>() }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        // Начинаем новый путь с одной точки
                        drawingPaths.add(
                            DrawingPath(
                                points = listOf(PathPoint(offset.x, offset.y, isMoveTo = true)),
                                brush = SolidColor(Color.Black),
                                strokeWidth = 5f
                            )
                        )
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val lastPath = drawingPaths.lastOrNull()
                        if (lastPath != null) {
                            // Создаем новый список точек, добавляя новую
                            val updatedPoints = lastPath.points + PathPoint(change.position.x, change.position.y)
                            // Заменяем старый путь новым, чтобы вызвать рекомпозицию
                            drawingPaths[drawingPaths.lastIndex] = lastPath.copy(points = updatedPoints)
                        }
                    }
                )
            }
    ) { 
        drawingPaths.forEach { drawingPath ->
            if (drawingPath.points.isNotEmpty()) {
                drawPath(
                    path = drawingPath.createComposePath(), // Используем метод
                    brush = drawingPath.brush, // Используем кисть из модели
                    style = Stroke(
                        width = drawingPath.strokeWidth,
                        cap = StrokeCap.Round, // Сглаживаем концы линий
                        join = StrokeJoin.Round // Сглаживаем углы
                    )
                )
            }
        }
    }
}
