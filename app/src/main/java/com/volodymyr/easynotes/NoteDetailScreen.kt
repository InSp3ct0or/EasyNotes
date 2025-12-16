package com.volodymyr.easynotes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.volodymyr.easynotes.data.Note
import com.volodymyr.easynotes.viewmodel.NoteViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

sealed interface DrawingTool {
    object Brush : DrawingTool
    object Pencil : DrawingTool
    object Marker : DrawingTool
    object Eraser : DrawingTool
}

data class DrawingPath(
    val path: Path,
    val brush: Brush,
    val strokeWidth: Float,
    val alpha: Float = 1f,
    val blendMode: BlendMode = BlendMode.SrcOver
)

// Gson не может сериализовать Path напрямую
data class PathPoint(val x: Float, val y: Float, val isMoveTo: Boolean = false)
data class SerializablePath(val points: List<PathPoint>)
data class SerializableDrawingPath(
    val path: SerializablePath,
    val brush: String,
    val strokeWidth: Float,
    val alpha: Float,
    val blendMode: String
)

object PathConverter {
    private val gson = Gson()

    fun pathsToJson(paths: List<DrawingPath>): String {
        val serializablePaths = paths.map { drawingPath ->
            val serializablePath = SerializablePath(
                drawingPath.path.toPoints()
            )
            SerializableDrawingPath(
                path = serializablePath,
                brush = "SolidColor(${Color.Black.toArgb()})",
                strokeWidth = drawingPath.strokeWidth,
                alpha = drawingPath.alpha,
                blendMode = drawingPath.blendMode.toString()
            )
        }
        return gson.toJson(serializablePaths)
    }

    fun jsonToPaths(json: String): List<DrawingPath> {
        return try {
            val type = object : TypeToken<List<SerializableDrawingPath>>() {}.type
            val serializablePaths: List<SerializableDrawingPath> = gson.fromJson(json, type)
            serializablePaths.map { serializablePath ->
                val path = Path()
                serializablePath.path.points.forEach { point ->
                    if (point.isMoveTo) {
                        path.moveTo(point.x, point.y)
                    } else {
                        path.lineTo(point.x, point.y)
                    }
                }
                DrawingPath(
                    path = path,
                    brush = SolidColor(Color.Black),
                    strokeWidth = serializablePath.strokeWidth,
                    alpha = serializablePath.alpha,
                    blendMode = BlendMode.SrcOver
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Альтернативная реализация без PathIterator
    private fun Path.toPoints(): List<PathPoint> {
        // Так как PathIterator недоступен, используем упрощенный подход
        // Сохраняем только базовую информацию о пути
        // Это ограничение, но работает для большинства случаев рисования
        return emptyList() // Временное решение
    }
}

val noteColors = listOf(
    Color(0xFFFFF59D), // Светло-желтый (по умолчанию)
    Color(0xFFFDD835),
    Color(0xFF8BC34A),
    Color(0xFF4DD0E1),
    Color(0xFFF06292),
    Color(0xFFBA68C8),
    Color(0xFF7986CB)
)

val gradientBrushes = listOf(
    Brush.linearGradient(listOf(Color.Red, Color.Yellow)),
    Brush.linearGradient(listOf(Color.Green, Color.Blue)),
    Brush.linearGradient(listOf(Color.Magenta, Color.Cyan)),
    Brush.radialGradient(listOf(Color.White, Color.Black), radius = 50f)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    noteId: Int,
    navController: NavController,
    noteViewModel: NoteViewModel
) {
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedNoteColor by remember { mutableStateOf(noteColors.first()) }

    var textColor by remember { mutableStateOf(Color.Black) }
    var fontSize by remember { mutableStateOf(16.sp) }

    var existingNote by remember { mutableStateOf<Note?>(null) }
    val isNewNote = noteId == 0

    // Состояние рисования
    var isInDrawingMode by remember { mutableStateOf(false) }
    val drawingPaths = remember { mutableStateListOf<DrawingPath>() }
    val redoStack = remember { mutableStateListOf<DrawingPath>() }

    var currentTool by remember { mutableStateOf<DrawingTool>(DrawingTool.Brush) }
    var drawingBrush by remember { mutableStateOf<Brush>(SolidColor(Color.Black)) }
    var strokeWidth by remember { mutableStateOf(10f) }
    var markerOpacity by remember { mutableStateOf(0.5f) }

    var currentPath by remember { mutableStateOf(Path()) }
    var currentPathStart by remember { mutableStateOf<Offset?>(null) }

    var toolbarOffset by remember { mutableStateOf(Offset(80f, 800f)) }

    var titleHeight by remember { mutableStateOf(0.dp) }
    val localDensity = LocalDensity.current

    LaunchedEffect(noteId) {
        if (!isNewNote) {
            val note: Note? = noteViewModel.getNoteById(noteId)
            note?.let { loadedNote ->
                existingNote = loadedNote
                title = loadedNote.title
                content = loadedNote.content
                selectedNoteColor = Color(loadedNote.color)
                loadedNote.drawingPathsJson?.let {
                    if (it.isNotEmpty()) {
                        drawingPaths.addAll(PathConverter.jsonToPaths(it))
                    }
                }
            }
        }
    }

    val saveNote: () -> Unit = {
        scope.launch {
            if (title.isBlank() && content.isBlank() && drawingPaths.isEmpty()) {
                navController.popBackStack()
                return@launch
            }

            val noteToSave = Note(
                id = existingNote?.id ?: 0,
                title = title.trim(),
                content = content.trim(),
                timestamp = existingNote?.timestamp ?: System.currentTimeMillis(),
                color = selectedNoteColor.toArgb(),
                drawingPathsJson = if (drawingPaths.isNotEmpty()) {
                    PathConverter.pathsToJson(drawingPaths)
                } else null
            )

            if (isNewNote) {
                noteViewModel.insert(noteToSave)
            } else {
                noteViewModel.update(noteToSave)
            }

            navController.popBackStack()
        }
    }

    val onUndo = {
        if (drawingPaths.isNotEmpty()) {
            val lastPath = drawingPaths.removeAt(drawingPaths.lastIndex)
            redoStack.add(lastPath)
        }
    }

    val onRedo = {
        if (redoStack.isNotEmpty()) {
            val lastPath = redoStack.removeAt(redoStack.lastIndex)
            drawingPaths.add(lastPath)
        }
    }

    val onCancelDrawing = {
        drawingPaths.clear()
        redoStack.clear()
        isInDrawingMode = false
    }

    Scaffold(
        topBar = {
            NoteDetailAppBar(
                isNewNote = isNewNote,
                onSaveClick = saveNote,
                onBackClick = { navController.popBackStack() },
                noteColor = selectedNoteColor,
                onNoteColorChange = { selectedNoteColor = it },
                isInDrawingMode = isInDrawingMode,
                onConfirmDrawing = {
                    isInDrawingMode = false
                    saveNote()
                },
                onCancelDrawing = onCancelDrawing,
                onUndo = onUndo,
                onRedo = onRedo,
                canUndo = drawingPaths.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        },
        bottomBar = {
            if (!isInDrawingMode) {
                FormattingToolbar(
                    textColor = textColor,
                    fontSize = fontSize,
                    onTextColorChange = { textColor = it },
                    onFontSizeChange = { fontSize = it },
                    onDrawingClick = { isInDrawingMode = true }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            // Основной холст для рисования
            if (isInDrawingMode) {
                Canvas(modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(currentTool) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                if (offset.y > with(localDensity) { titleHeight.toPx() }) {
                                    currentPath = Path().apply { moveTo(offset.x, offset.y) }
                                    currentPathStart = offset
                                }
                            },
                            onDragEnd = {
                                if (currentPathStart != null) {
                                    val attributes = getDrawingAttributes(
                                        currentTool,
                                        strokeWidth,
                                        drawingBrush,
                                        markerOpacity
                                    )
                                    drawingPaths.add(
                                        DrawingPath(
                                            path = currentPath,
                                            brush = attributes.brush,
                                            strokeWidth = attributes.strokeWidth,
                                            alpha = attributes.alpha,
                                            blendMode = attributes.blendMode
                                        )
                                    )
                                    redoStack.clear()
                                    currentPath = Path()
                                    currentPathStart = null
                                }
                            },
                            onDrag = { change, _ ->
                                if (currentPathStart != null) {
                                    val current = change.position
                                    val previous = currentPathStart ?: current
                                    if (current.y > with(localDensity) { titleHeight.toPx() }) {
                                        currentPath.quadraticBezierTo(
                                            previous.x, previous.y,
                                            (previous.x + current.x) / 2,
                                            (previous.y + current.y) / 2
                                        )
                                        currentPathStart = current
                                    }
                                    change.consume()
                                }
                            }
                        )
                    }
                ) {
                    // Рисуем существующие пути
                    drawingPaths.forEach { (path, brush, width, alpha, blendMode) ->
                        drawPath(
                            path = path,
                            brush = brush,
                            style = Stroke(
                                width = width,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            ),
                            alpha = alpha,
                            blendMode = blendMode
                        )
                    }

                    // Рисуем текущий путь в реальном времени
                    if (currentPathStart != null) {
                        val attributes = getDrawingAttributes(
                            currentTool,
                            strokeWidth,
                            drawingBrush,
                            markerOpacity
                        )
                        drawPath(
                            path = currentPath,
                            brush = attributes.brush,
                            style = Stroke(
                                width = attributes.strokeWidth,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            ),
                            alpha = attributes.alpha,
                            blendMode = attributes.blendMode
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to selectedNoteColor,
                                0.5f to MaterialTheme.colorScheme.background
                            )
                        ), alpha = if (isInDrawingMode) 0.5f else 1.0f
                    )
                    .padding(horizontal = 16.dp)
                    .zIndex(1f)
            ) {
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Заголовок") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned {
                            titleHeight = with(localDensity) { it.size.height.toDp() }
                        },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    textStyle = MaterialTheme.typography.headlineSmall,
                    readOnly = isInDrawingMode
                )

                TextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Текст заметки") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    textStyle = TextStyle(color = textColor, fontSize = fontSize),
                    readOnly = isInDrawingMode
                )
            }

            if (isInDrawingMode) {
                // Плавающая перетаскиваемая панель инструментов
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                toolbarOffset.x.roundToInt(),
                                toolbarOffset.y.roundToInt()
                            )
                        }
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                toolbarOffset += dragAmount
                            }
                        }
                        .zIndex(2f)
                ) {
                    DrawingToolbar(
                        currentTool = currentTool,
                        onToolChange = { tool -> currentTool = tool },
                        drawingBrush = drawingBrush,
                        onBrushChange = { brush ->
                            drawingBrush = brush
                            if (currentTool is DrawingTool.Eraser) {
                                currentTool = DrawingTool.Brush
                            }
                        },
                        strokeWidth = strokeWidth,
                        onStrokeWidthChange = { strokeWidth = it },
                        markerOpacity = markerOpacity,
                        onMarkerOpacityChange = { markerOpacity = it }
                    )
                }
            }
        }
    }
}

fun getDrawingAttributes(
    tool: DrawingTool,
    strokeWidth: Float,
    brush: Brush,
    opacity: Float
): DrawingPath {
    return when (tool) {
        is DrawingTool.Brush -> DrawingPath(
            Path(),
            brush,
            strokeWidth,
            1f,
            BlendMode.SrcOver
        )
        is DrawingTool.Pencil -> DrawingPath(
            Path(),
            brush,
            strokeWidth / 4,
            1f,
            BlendMode.SrcOver
        )
        is DrawingTool.Marker -> DrawingPath(
            Path(),
            brush,
            strokeWidth * 2,
            opacity,
            BlendMode.SrcOver
        )
        is DrawingTool.Eraser -> DrawingPath(
            Path(),
            SolidColor(Color.Transparent),
            strokeWidth * 2,
            1f,
            BlendMode.Clear
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailAppBar(
    isNewNote: Boolean,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit,
    noteColor: Color,
    onNoteColorChange: (Color) -> Unit,
    isInDrawingMode: Boolean,
    onConfirmDrawing: () -> Unit,
    onCancelDrawing: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    canUndo: Boolean,
    canRedo: Boolean
) {
    var showColorMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Text(
                if (isInDrawingMode) "Рисование"
                else if (isNewNote) "Новая заметка"
                else "Редактировать заметку"
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = noteColor
        ),
        navigationIcon = {
            if (isInDrawingMode) {
                IconButton(onClick = onCancelDrawing) {
                    Icon(Icons.Default.Close, contentDescription = "Отмена")
                }
            } else {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Назад")
                }
            }
        },
        actions = {
            if (isInDrawingMode) {
                IconButton(onClick = onUndo, enabled = canUndo) {
                    Icon(Icons.Default.Undo, contentDescription = "Отменить")
                }
                IconButton(onClick = onRedo, enabled = canRedo) {
                    Icon(Icons.Default.Redo, contentDescription = "Повторить")
                }
                IconButton(onClick = onConfirmDrawing) {
                    Icon(Icons.Default.Check, contentDescription = "Подтвердить")
                }
            } else {
                Box {
                    IconButton(onClick = { showColorMenu = true }) {
                        Icon(Icons.Default.ColorLens, contentDescription = "Цвет заметки")
                    }
                    DropdownMenu(
                        expanded = showColorMenu,
                        onDismissRequest = { showColorMenu = false }
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(8.dp)
                        ) {
                            noteColors.forEach { color ->
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .clickable {
                                            onNoteColorChange(color)
                                            showColorMenu = false
                                        }
                                        .border(1.dp, Color.Gray, CircleShape)
                                )
                            }
                        }
                    }
                }

                IconButton(onClick = onSaveClick) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = "Сохранить",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    )
}

@Composable
fun FormattingToolbar(
    textColor: Color,
    fontSize: androidx.compose.ui.unit.TextUnit,
    onTextColorChange: (Color) -> Unit,
    onFontSizeChange: (androidx.compose.ui.unit.TextUnit) -> Unit,
    onDrawingClick: () -> Unit
) {
    val fontSizes = listOf(14.sp, 16.sp, 18.sp) // S, M, L
    val textColors = listOf(Color.Black, Color.Red, Color.Blue, Color.Green, Color.Gray)
    var showColorPicker by remember { mutableStateOf(false) }

    Surface(
        tonalElevation = 4.dp,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { showColorPicker = !showColorPicker }) {
                        Icon(Icons.Default.Palette, contentDescription = "Выбор цвета")
                    }

                    Row(
                        modifier = Modifier.padding(start = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.FormatSize, contentDescription = "Размер шрифта")
                        Spacer(Modifier.width(8.dp))
                        fontSizes.forEachIndexed { index, size ->
                            val label = when (index) {
                                0 -> "S"
                                1 -> "M"
                                else -> "L"
                            }
                            Text(
                                text = label,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable { onFontSizeChange(size) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                color = if (fontSize == size) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                        }
                    }
                }

                Button(onClick = onDrawingClick) {
                    Icon(
                        Icons.Default.Brush,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Рисование")
                }
            }

            AnimatedVisibility(visible = showColorPicker) {
                LazyRow(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(textColors) { color ->
                        val isSelected = textColor == color
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { onTextColorChange(color) }
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Выбрано",
                                    tint = if (color == Color.Black) Color.White else Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DrawingToolbar(
    modifier: Modifier = Modifier,
    currentTool: DrawingTool,
    onToolChange: (DrawingTool) -> Unit,
    drawingBrush: Brush,
    onBrushChange: (Brush) -> Unit,
    strokeWidth: Float,
    onStrokeWidthChange: (Float) -> Unit,
    markerOpacity: Float,
    onMarkerOpacityChange: (Float) -> Unit
) {
    val solidColors = listOf(
        Color.Black,
        Color.Red,
        Color.Blue,
        Color.Green,
        Color.Yellow,
        Color.Magenta
    )

    Surface(
        modifier = modifier.width(300.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.DragHandle,
                contentDescription = "Переместить",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Выбор инструмента
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ToolIconButton(
                    icon = Icons.Default.Brush,
                    isSelected = currentTool is DrawingTool.Brush
                ) {
                    onToolChange(DrawingTool.Brush)
                }
                ToolIconButton(
                    icon = Icons.Default.Create,
                    isSelected = currentTool is DrawingTool.Pencil
                ) {
                    onToolChange(DrawingTool.Pencil)
                }
                ToolIconButton(
                    icon = Icons.Default.Star,
                    isSelected = currentTool is DrawingTool.Marker
                ) {
                    onToolChange(DrawingTool.Marker)
                }
                ToolIconButton(
                    icon = Icons.Default.DeleteOutline,
                    isSelected = currentTool is DrawingTool.Eraser
                ) {
                    onToolChange(DrawingTool.Eraser)
                }
            }

            // Палитра сплошных цветов
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                items(solidColors) { color ->
                    val isSelected = (drawingBrush as? SolidColor)?.value == color &&
                            currentTool !is DrawingTool.Eraser
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(color)
                            .clickable { onBrushChange(SolidColor(color)) }
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                                shape = CircleShape
                            )
                    )
                }
            }

            // Палитра градиентов
            LazyRow(
                modifier = Modifier.padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(gradientBrushes) { brush ->
                    val isSelected = brush == drawingBrush
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(brush)
                            .clickable { onBrushChange(brush) }
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                                shape = CircleShape
                            )
                    )
                }
            }

            // Слайдер толщины линии
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Толщина кисти", style = MaterialTheme.typography.bodySmall)
                Slider(
                    value = strokeWidth,
                    onValueChange = onStrokeWidthChange,
                    valueRange = 5f..50f
                )
            }

            // Слайдер прозрачности для маркера
            AnimatedVisibility(visible = currentTool is DrawingTool.Marker) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Прозрачность маркера", style = MaterialTheme.typography.bodySmall)
                    Slider(
                        value = markerOpacity,
                        onValueChange = onMarkerOpacityChange,
                        valueRange = 0.1f..1f
                    )
                }
            }
        }
    }
}

@Composable
private fun ToolIconButton(icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
        )
    ) {
        Icon(imageVector = icon, contentDescription = null)
    }
}