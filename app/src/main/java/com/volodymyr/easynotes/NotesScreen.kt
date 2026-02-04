package com.volodymyr.easynotes

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.volodymyr.easynotes.data.Note
import com.volodymyr.easynotes.viewmodel.SortOrder
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    notes: List<Note>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    sortOrder: SortOrder,
    onSortOrderChange: (SortOrder) -> Unit,
    isDarkMode: Boolean,
    onDarkModeToggle: (Boolean) -> Unit,
    onNoteClick: (Note) -> Unit,
    onDeleteClick: (Note) -> Unit,
    onAddNoteClick: () -> Unit,
    onLanguageSelected: (String) -> Unit
) {
    var isSearchActive by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    if (showLanguageDialog) {
        LanguageDialog(
            onDismiss = { showLanguageDialog = false },
            onLanguageSelected = onLanguageSelected
        )
    }

    Scaffold(
        topBar = {
            NotesAppBar(
                noteCount = notes.size,
                isSearchActive = isSearchActive,
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                onSearchToggle = {
                    isSearchActive = !isSearchActive
                    if (!isSearchActive) onSearchQueryChange("")
                },
                sortOrder = sortOrder,
                onSortOrderChange = onSortOrderChange,
                isDarkMode = isDarkMode,
                onDarkModeToggle = onDarkModeToggle,
                onLanguageClick = { showLanguageDialog = true }
            )
        },
        floatingActionButton = { NoteFAB(onAddNoteClick = onAddNoteClick) },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        if (notes.isEmpty()) {
            EmptyState(paddingValues, isSearchActive)
        } else {
            NotesListContent(notes, isDarkMode, onNoteClick, onDeleteClick, paddingValues)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesAppBar(
    noteCount: Int,
    isSearchActive: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchToggle: () -> Unit,
    sortOrder: SortOrder,
    onSortOrderChange: (SortOrder) -> Unit,
    isDarkMode: Boolean,
    onDarkModeToggle: (Boolean) -> Unit,
    onLanguageClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            focusRequester.requestFocus()
        }
    }

    TopAppBar(
        title = {
            if (isSearchActive) {
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text(stringResource(R.string.search_notes_placeholder), fontSize = 18.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    textStyle = LocalTextStyle.current.copy(fontSize = 18.sp)
                )
            } else {
                Column(Modifier.padding(top = 8.dp)) {
                    Text(stringResource(R.string.all_notes), fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    Text(
                        text = LocalContext.current.resources.getQuantityString(R.plurals.notes_count, noteCount, noteCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        navigationIcon = {
            if (isSearchActive) {
                IconButton(onClick = onSearchToggle) {
                    Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                }
            }
        },
        actions = {
            if (!isSearchActive) {
                IconButton(onClick = onSearchToggle) {
                    Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.search))
                }
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.options))
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.sort_by_date)) },
                            onClick = { 
                                onSortOrderChange(SortOrder.BY_DATE)
                                showMenu = false 
                            },
                            leadingIcon = { 
                                Icon(
                                    Icons.Default.Event, 
                                    contentDescription = null,
                                    tint = if (sortOrder == SortOrder.BY_DATE) MaterialTheme.colorScheme.primary else LocalContentColor.current
                                ) 
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.sort_by_title)) },
                            onClick = { 
                                onSortOrderChange(SortOrder.BY_TITLE)
                                showMenu = false 
                            },
                            leadingIcon = { 
                                Icon(
                                    Icons.Default.SortByAlpha, 
                                    contentDescription = null,
                                    tint = if (sortOrder == SortOrder.BY_TITLE) MaterialTheme.colorScheme.primary else LocalContentColor.current
                                ) 
                            }
                        )
                        Divider()
                        DropdownMenuItem(
                            text = { 
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(stringResource(R.string.dark_theme))
                                    Switch(
                                        checked = isDarkMode,
                                        onCheckedChange = { 
                                            onDarkModeToggle(it)
                                        }
                                    )
                                }
                            },
                            onClick = { onDarkModeToggle(!isDarkMode) },
                            leadingIcon = { 
                                Icon(
                                    if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode, 
                                    contentDescription = null
                                ) 
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.language)) },
                            onClick = {
                                showMenu = false
                                onLanguageClick()
                            },
                            leadingIcon = { Icon(Icons.Default.Language, contentDescription = null) }
                        )
                    }
                }
            } else if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { onSearchQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.clear))
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        )
    )
}

@Composable
fun LanguageDialog(
    onDismiss: () -> Unit,
    onLanguageSelected: (String) -> Unit
) {
    val languages = mapOf(
        "en" to "English",
        "ru" to "Русский",
        "cs" to "Čeština"
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_language)) },
        text = {
            Column {
                languages.forEach { (locale, name) ->
                    Text(
                        text = name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onLanguageSelected(locale)
                                onDismiss()
                            }
                            .padding(vertical = 12.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun NoteFAB(onAddNoteClick: () -> Unit) {
    FloatingActionButton(onClick = onAddNoteClick) {
        Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_note))
    }
}

@Composable
fun EmptyState(paddingValues: PaddingValues, isSearchActive: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isSearchActive) stringResource(R.string.nothing_found) else stringResource(R.string.empty_state_message),
            color = Color.Gray
        )
    }
}

@Composable
fun NotesListContent(
    notes: List<Note>,
    isDarkMode: Boolean,
    onNoteClick: (Note) -> Unit,
    onDeleteClick: (Note) -> Unit,
    paddingValues: PaddingValues
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        contentPadding = paddingValues,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(notes, key = { it.id }) { note ->
            NoteCard(note, isDarkMode, onNoteClick, onDeleteClick)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteCard(
    note: Note,
    isDarkMode: Boolean,
    onNoteClick: (Note) -> Unit,
    onDeleteClick: (Note) -> Unit
) {
    val cardAlpha = if (isDarkMode) 0.5f else 0.3f
    val containerColor = Color(note.color).copy(alpha = cardAlpha)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp)
            .padding(4.dp),
        onClick = { onNoteClick(note) },
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {
        Column {
            if (note.imagePath != null) {
                NoteImageSection(note.imagePath)
            }
            
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = note.title.ifEmpty { stringResource(R.string.untitled) },
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = note.content,
                    style = TextStyle(
                        color = Color(note.textColor),
                        fontSize = note.fontSize.sp
                    ),
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.created_at, SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(java.util.Date(note.timestamp))),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IconButton(onClick = { onDeleteClick(note) }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete_note),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NoteImageSection(imagePath: String) {
    var rotationAngle by remember { mutableFloatStateOf(0f) }
    val animatedRotation by animateFloatAsState(targetValue = rotationAngle, label = "imageRotation")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(8.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        AsyncImage(
            model = imagePath,
            contentDescription = stringResource(R.string.note_image),
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationZ = animatedRotation
                },
            contentScale = ContentScale.Crop
        )
        
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .size(36.dp)
                .clickable { rotationAngle += 90f },
            shape = CircleShape,
            color = Color.Black.copy(alpha = 0.5f),
            contentColor = Color.White
        ) {
            Icon(
                imageVector = Icons.Default.RotateRight,
                contentDescription = stringResource(R.string.rotate),
                modifier = Modifier.padding(6.dp)
            )
        }
    }
}
