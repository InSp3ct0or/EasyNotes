package com.volodymyr.easynotes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.volodymyr.easynotes.data.Note
import com.volodymyr.easynotes.viewmodel.NoteViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    noteId: Int,
    navController: NavController,
    noteViewModel: NoteViewModel
) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val density = LocalDensity.current

    // Используем rememberSaveable для сохранения текста при повороте экрана
    var title by rememberSaveable { mutableStateOf("") }
    var content by rememberSaveable { mutableStateOf("") }
    
    // Для сложных типов данных, таких как Color, используем Argb для сохранения
    var selectedNoteColorArgb by rememberSaveable { mutableIntStateOf(noteColors.first().toArgb()) }
    val selectedNoteColor = Color(selectedNoteColorArgb)
    
    var textColorArgb by rememberSaveable { mutableIntStateOf(Color.Black.toArgb()) }
    val textColor = Color(textColorArgb)
    
    var fontSizeValue by rememberSaveable { mutableFloatStateOf(16f) }
    val fontSize = fontSizeValue.sp

    var existingNote by remember { mutableStateOf<Note?>(null) }
    val isNewNote = noteId == 0

    var isInDrawingMode by remember { mutableStateOf(false) }
    val drawingPaths = remember { mutableStateListOf<DrawingPath>() }
    val redoStack = remember { mutableStateListOf<DrawingPath>() }

    var currentTool by remember { mutableStateOf<DrawingTool>(DrawingTool.Brush) }
    var drawingBrush by remember { mutableStateOf<Brush>(SolidColor(Color.Black)) }
    var strokeWidth by remember { mutableFloatStateOf(10f) }
    var markerOpacity by remember { mutableFloatStateOf(0.5f) }

    val currentPoints = remember { mutableStateListOf<PathPoint>() }
    var toolbarOffset by remember { mutableStateOf(Offset(80f, 800f)) }

    var boxHeight by remember { mutableFloatStateOf(0f) }
    var titleHeight by remember { mutableFloatStateOf(0f) }

    // Вычисляем минимальную высоту области контента, чтобы она занимала весь экран ниже заголовка
    val minContentHeight = remember(boxHeight, titleHeight) {
        val availableHeight = boxHeight - titleHeight
        if (availableHeight > 0) {
            with(density) { availableHeight.toDp() }
        } else {
            300.dp // Значение по умолчанию, если высота еще не измерена
        }
    }

    val gradient = Brush.verticalGradient(
        colors = listOf(
            selectedNoteColor.copy(alpha = 0.7f),
            Color.Transparent
        ),
        endY = boxHeight / 2
    )

    LaunchedEffect(noteId) {
        if (!isNewNote && title.isEmpty() && content.isEmpty()) {
            val note = noteViewModel.getNoteById(noteId)
            note?.let { loadedNote ->
                existingNote = loadedNote
                title = loadedNote.title
                content = loadedNote.content
                selectedNoteColorArgb = loadedNote.color
                loadedNote.drawingPathsJson?.let {
                    if (it.isNotEmpty()) {
                        drawingPaths.clear()
                        drawingPaths.addAll(PathConverter.jsonToPaths(it))
                    }
                }
            }
        }
    }

    val saveNote: () -> Unit = {
        scope.launch {
            val noteToSave = Note(
                id = existingNote?.id ?: 0,
                title = title.trim(),
                content = content.trim(),
                timestamp = existingNote?.timestamp ?: System.currentTimeMillis(),
                color = selectedNoteColorArgb,
                drawingPathsJson = if (drawingPaths.isNotEmpty()) PathConverter.pathsToJson(drawingPaths) else null,
                imagePath = existingNote?.imagePath // Сохраняем путь к изображению
            )
            if (isNewNote) noteViewModel.insert(noteToSave) else noteViewModel.update(noteToSave)
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            NoteDetailAppBar(
                isNewNote = isNewNote,
                onSaveClick = saveNote,
                onBackClick = { navController.popBackStack() },
                noteColor = selectedNoteColor,
                onNoteColorChange = { selectedNoteColorArgb = it.toArgb() },
                isInDrawingMode = isInDrawingMode,
                onConfirmDrawing = { isInDrawingMode = false },
                onCancelDrawing = { isInDrawingMode = false },
                onUndo = { if (drawingPaths.isNotEmpty()) redoStack.add(drawingPaths.removeAt(drawingPaths.lastIndex)) },
                onRedo = { if (redoStack.isNotEmpty()) drawingPaths.add(redoStack.removeAt(redoStack.lastIndex)) },
                canUndo = drawingPaths.isNotEmpty(),
                canRedo = redoStack.isNotEmpty()
            )
        },
        bottomBar = {
            if (!isInDrawingMode) {
                FormattingToolbar(
                    textColor = textColor,
                    fontSize = fontSize,
                    onTextColorChange = { textColorArgb = it.toArgb() },
                    onFontSizeChange = { fontSizeValue = it.value },
                    onDrawingClick = { isInDrawingMode = true }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .onGloballyPositioned { coordinates ->
                    boxHeight = coordinates.size.height.toFloat()
                }
                .background(gradient)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState, enabled = !isInDrawingMode)
            ) {
                // Поле "Тема" (Title)
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    enabled = !isInDrawingMode,
                    label = { Text("Тема") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .onGloballyPositioned { coordinates ->
                            titleHeight = coordinates.size.height.toFloat()
                        },
                    textStyle = MaterialTheme.typography.headlineSmall,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    )
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = minContentHeight)
                ) {
                    // СЛОЙ РИСОВАНИЯ
                    Canvas(
                        modifier = Modifier
                            .matchParentSize() // Теперь Canvas всегда равен размеру Box (минимум до низа экрана)
                            .zIndex(if (isInDrawingMode) 1f else 0f)
                            .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                            .pointerInput(isInDrawingMode) {
                                if (!isInDrawingMode) return@pointerInput
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        currentPoints.clear()
                                        currentPoints.add(PathPoint(offset.x, offset.y, isMoveTo = true))
                                    },
                                    onDrag = { change, _ ->
                                        change.consume()
                                        currentPoints.add(PathPoint(change.position.x, change.position.y))
                                    },
                                    onDragEnd = {
                                        if (currentPoints.isNotEmpty()) {
                                            val attr = getDrawingAttributes(currentTool, strokeWidth, drawingBrush, markerOpacity)
                                            drawingPaths.add(DrawingPath(currentPoints.toList(), attr.brush, attr.strokeWidth, attr.alpha, attr.blendMode))
                                            currentPoints.clear()
                                            redoStack.clear()
                                        }
                                    }
                                )
                            }
                    ) {
                        drawingPaths.forEach { dp ->
                            drawPath(
                                path = dp.createComposePath(),
                                brush = dp.brush,
                                style = Stroke(width = dp.strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round),
                                alpha = dp.alpha,
                                blendMode = dp.blendMode
                            )
                        }

                        if (currentPoints.isNotEmpty()) {
                            val attr = getDrawingAttributes(currentTool, strokeWidth, drawingBrush, markerOpacity)
                            val tempPath = Path().apply {
                                currentPoints.forEachIndexed { i, p ->
                                    if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
                                }
                            }
                            drawPath(
                                path = tempPath,
                                brush = attr.brush,
                                style = Stroke(width = attr.strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round),
                                alpha = attr.alpha,
                                blendMode = attr.blendMode
                            )
                        }
                    }

                    // СЛОЙ ТЕКСТА (КОНТЕНТ)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .zIndex(if (isInDrawingMode) 0f else 1f)
                    ) {
                        TextField(
                            value = content,
                            onValueChange = { content = it },
                            enabled = !isInDrawingMode,
                            placeholder = { Text("Начните писать...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = minContentHeight),
                            textStyle = TextStyle(color = textColor, fontSize = fontSize),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }

            if (isInDrawingMode) {
                Box(
                    modifier = Modifier
                        .offset { IntOffset(toolbarOffset.x.roundToInt(), toolbarOffset.y.roundToInt()) }
                        .zIndex(2f)
                        .pointerInput(Unit) {
                            detectDragGestures { change, drag ->
                                change.consume()
                                toolbarOffset += drag
                            }
                        }
                ) {
                    DrawingToolbar(
                        currentTool = currentTool,
                        onToolChange = { currentTool = it },
                        drawingBrush = drawingBrush,
                        onBrushChange = { 
                            drawingBrush = it
                            if (currentTool is DrawingTool.Eraser) currentTool = DrawingTool.Brush
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
