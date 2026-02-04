package com.volodymyr.easynotes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

fun getDrawingAttributes(
    tool: DrawingTool,
    strokeWidth: Float,
    brush: Brush,
    opacity: Float
): DrawingPath {
    return when (tool) {
        is DrawingTool.Brush -> DrawingPath(
            emptyList(),
            brush,
            strokeWidth,
            1f,
            BlendMode.SrcOver
        )
        is DrawingTool.Pencil -> DrawingPath(
            emptyList(),
            brush,
            strokeWidth / 4,
            1f,
            BlendMode.SrcOver
        )
        is DrawingTool.Marker -> DrawingPath(
            emptyList(),
            brush,
            strokeWidth * 2,
            opacity,
            BlendMode.SrcOver
        )
        is DrawingTool.Eraser -> DrawingPath(
            emptyList(),
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
                if (isInDrawingMode) stringResource(R.string.drawing)
                else if (isNewNote) stringResource(R.string.new_note)
                else stringResource(R.string.edit_note)
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = noteColor
        ),
        navigationIcon = {
            if (isInDrawingMode) {
                IconButton(onClick = onCancelDrawing) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cancel))
                }
            } else {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                }
            }
        },
        actions = {
            if (isInDrawingMode) {
                IconButton(onClick = onUndo, enabled = canUndo) {
                    Icon(Icons.Default.Undo, contentDescription = stringResource(R.string.undo))
                }
                IconButton(onClick = onRedo, enabled = canRedo) {
                    Icon(Icons.Default.Redo, contentDescription = stringResource(R.string.redo))
                }
                IconButton(onClick = onConfirmDrawing) {
                    Icon(Icons.Default.Check, contentDescription = stringResource(R.string.confirm))
                }
            } else {
                Box {
                    IconButton(onClick = { showColorMenu = true }) {
                        Icon(Icons.Default.ColorLens, contentDescription = stringResource(R.string.note_color))
                    }
                    DropdownMenu(
                        expanded = showColorMenu,
                        onDismissRequest = { showColorMenu = false }
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = stringResource(R.string.color),
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                }

                IconButton(onClick = onSaveClick) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = stringResource(R.string.save),
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
    val fontSizes = listOf(14.sp, 16.sp, 18.sp)
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
                        Icon(Icons.Default.Palette, contentDescription = stringResource(R.string.color_picker))
                    }

                    Row(
                        modifier = Modifier.padding(start = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.FormatSize, contentDescription = stringResource(R.string.font_size))
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
                    Icon(Icons.Default.Brush, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.drawing))
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
                                    contentDescription = stringResource(R.string.selected),
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
    val solidColors = listOf(Color.Black, Color.Red, Color.Blue, Color.Green, Color.Yellow, Color.Magenta)

    Surface(
        modifier = modifier.width(320.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Filled.DragHandle, contentDescription = stringResource(R.string.drag_handle), tint = MaterialTheme.colorScheme.onSurfaceVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ToolIconButton(
                    icon = Icons.Default.Brush,
                    label = stringResource(R.string.brush),
                    isSelected = currentTool is DrawingTool.Brush
                ) { onToolChange(DrawingTool.Brush) }
                
                ToolIconButton(
                    icon = Icons.Default.Create,
                    label = stringResource(R.string.pencil),
                    isSelected = currentTool is DrawingTool.Pencil
                ) { onToolChange(DrawingTool.Pencil) }
                
                ToolIconButton(
                    icon = Icons.Default.Star,
                    label = stringResource(R.string.marker),
                    isSelected = currentTool is DrawingTool.Marker
                ) { onToolChange(DrawingTool.Marker) }
                
                ToolIconButton(
                    icon = Icons.Default.DeleteOutline,
                    label = stringResource(R.string.eraser),
                    isSelected = currentTool is DrawingTool.Eraser
                ) { onToolChange(DrawingTool.Eraser) }
            }

            Divider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                items(solidColors) { color ->
                    val isSelected = (drawingBrush as? SolidColor)?.value == color && currentTool !is DrawingTool.Eraser
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

            LazyRow(
                modifier = Modifier.padding(vertical = 4.dp),
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

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Text(stringResource(R.string.stroke_width), style = MaterialTheme.typography.labelSmall)
                Slider(value = strokeWidth, onValueChange = onStrokeWidthChange, valueRange = 5f..50f)
            }

            AnimatedVisibility(visible = currentTool is DrawingTool.Marker) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.opacity), style = MaterialTheme.typography.labelSmall)
                    Slider(value = markerOpacity, onValueChange = onMarkerOpacityChange, valueRange = 0.1f..1f)
                }
            }
        }
    }
}

@Composable
private fun ToolIconButton(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(4.dp)
    ) {
        IconButton(
            onClick = onClick,
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.size(48.dp)
        ) {
            Icon(imageVector = icon, contentDescription = label)
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
