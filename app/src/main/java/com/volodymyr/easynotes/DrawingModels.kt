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
    fun createComposePath(): Path {
        val path = Path()
        points.forEachIndexed { i, point ->
            if (i == 0 || point.isMoveTo) path.moveTo(point.x, point.y)
            else path.lineTo(point.x, point.y)
        }
        return path
    }
}

// Модель для сохранения
data class SerializableDrawingPath(
    val points: List<PathPoint>,
    val strokeWidth: Float,
    val alpha: Float,
    val color: Int? = null,
    val gradientColors: List<Int>? = null
)

// ==================== КОНВЕРТЕР ПУТЕЙ ====================
object PathConverter {
    private val gson = Gson()

    fun pathsToJson(paths: List<DrawingPath>): String {
        val serializable = paths.map { path ->
            when (val b = path.brush) {
                is SolidColor -> {
                    SerializableDrawingPath(
                        points = path.points,
                        strokeWidth = path.strokeWidth,
                        alpha = path.alpha,
                        color = b.value.toArgb()
                    )
                }
                else -> {
                    // Пытаемся найти цвета градиента среди наших пресетов
                    val colors = findColorsForBrush(b)
                    if (colors.isNotEmpty()) {
                        SerializableDrawingPath(
                            points = path.points,
                            strokeWidth = path.strokeWidth,
                            alpha = path.alpha,
                            gradientColors = colors.map { it.toArgb() }
                        )
                    } else {
                        // Фолбек на черный цвет, если градиент не распознан
                        SerializableDrawingPath(
                            path.points, path.strokeWidth, path.alpha, Color.Black.toArgb()
                        )
                    }
                }
            }
        }
        return gson.toJson(serializable)
    }

    fun jsonToPaths(json: String): List<DrawingPath> {
        if (json.isEmpty()) return emptyList()
        return try {
            val type = object : TypeToken<List<SerializableDrawingPath>>() {}.type
            val list: List<SerializableDrawingPath> = gson.fromJson(json, type)
            list.map { s ->
                val brush = if (s.gradientColors != null && s.gradientColors.size >= 2) {
                    Brush.linearGradient(s.gradientColors.map { Color(it) })
                } else {
                    SolidColor(Color(s.color ?: Color.Black.toArgb()))
                }
                DrawingPath(s.points, brush, s.strokeWidth, s.alpha)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Сопоставляем кисть с нашими данными о цветах
    private fun findColorsForBrush(brush: Brush): List<Color> {
        // Проверяем наши предопределенные градиенты
        if (brush == gradientBrushes[0]) return listOf(Color.Red, Color.Yellow)
        if (brush == gradientBrushes[1]) return listOf(Color.Green, Color.Blue)
        if (brush == gradientBrushes[2]) return listOf(Color.Magenta, Color.Cyan)
        return emptyList()
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
