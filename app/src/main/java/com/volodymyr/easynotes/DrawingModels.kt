package com.volodymyr.easynotes

import androidx.compose.ui.graphics.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// ==================== ИНСТРУМЕНТЫ РИСОВАНИЯ ====================
sealed interface DrawingTool {
    object Brush : DrawingTool
    object Pencil : DrawingTool
    object Marker : DrawingTool
    object Eraser : DrawingTool
}

// ==================== МОДЕЛИ ДАННЫХ ====================

data class PathPoint(val x: Float, val y: Float, val isMoveTo: Boolean = false)

data class DrawingPath(
    val points: List<PathPoint>,
    val brush: Brush,
    val strokeWidth: Float,
    val alpha: Float = 1f,
    val blendMode: BlendMode = BlendMode.SrcOver
) {
    // Вспомогательный метод для отрисовки (не сохраняется в JSON)
    fun createComposePath(): Path {
        val path = Path()
        points.forEachIndexed { i, point ->
            if (i == 0 || point.isMoveTo) path.moveTo(point.x, point.y)
            else path.lineTo(point.x, point.y)
        }
        return path
    }
}

// Модель для сохранения (JSON не умеет в Brush)
data class SerializableDrawingPath(
    val points: List<PathPoint>,
    val color: Int,
    val strokeWidth: Float,
    val alpha: Float,
    val blendMode: Int
)

// ==================== КОНВЕРТЕР ПУТЕЙ ====================
object PathConverter {
    private val gson = Gson()

    fun pathsToJson(paths: List<DrawingPath>): String {
        val serializable = paths.map {
            val color = if (it.brush is SolidColor) it.brush.value.toArgb() else Color.Black.toArgb()
            SerializableDrawingPath(it.points, color, it.strokeWidth, it.alpha, 0)
        }
        return gson.toJson(serializable)
    }

    fun jsonToPaths(json: String): List<DrawingPath> {
        return try {
            val type = object : TypeToken<List<SerializableDrawingPath>>() {}.type
            val list: List<SerializableDrawingPath> = gson.fromJson(json, type)
            list.map {
                DrawingPath(it.points, SolidColor(Color(it.color)), it.strokeWidth, it.alpha)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}

val noteColors = listOf(
    Color(0xFFFFF59D), Color(0xFFFDD835), Color(0xFF8BC34A),
    Color(0xFF4DD0E1), Color(0xFFF06292), Color(0xFFBA68C8), Color(0xFF7986CB)
)

val gradientBrushes = listOf(
    Brush.linearGradient(listOf(Color.Red, Color.Yellow)),
    Brush.linearGradient(listOf(Color.Green, Color.Blue)),
    Brush.linearGradient(listOf(Color.Magenta, Color.Cyan))
)
