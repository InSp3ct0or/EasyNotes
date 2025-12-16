package com.volodymyr.easynotes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.volodymyr.easynotes.data.Note
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    notes: List<Note>,
    onNoteClick: (Note) -> Unit,
    onDeleteClick: (Note) -> Unit,
    onAddNoteClick: () -> Unit // НОВЫЙ КОЛБЭК ДЛЯ FAB
) {
    Scaffold(
        topBar = { NotesAppBar(notes.size) },
        // **ПОДКЛЮЧЕНИЕ FAB:** Передаем обработчик нажатия
        floatingActionButton = { NoteFAB(onAddNoteClick = onAddNoteClick) },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        if (notes.isEmpty()) {
            EmptyState(paddingValues)
        } else {
            NotesListContent(notes, onNoteClick, onDeleteClick, paddingValues)
        }
    }
}

// 1. Общая Структура Экрана (Header)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesAppBar(noteCount: Int) {
    TopAppBar(
        title = {
            Column(Modifier.padding(top = 8.dp)) {
                // Заголовок Экрана
                Text("Все заметки", fontWeight = FontWeight.Bold, fontSize = 24.sp)
                // Счетчик Заметок
                Text(
                    text = "${noteCount} замет${
                        when {
                            noteCount % 10 == 1 && noteCount % 100 != 11 -> "ка"
                            noteCount % 10 in 2..4 && noteCount % 100 !in 12..14 -> "ки"
                            else -> "ок"
                        }
                    }",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        actions = {
            // Кнопка Поиска
            IconButton(onClick = { /* TODO: Открыть поиск */ }) {
                Icon(Icons.Filled.Search, contentDescription = "Поиск")
            }
            // Кнопка Опций
            IconButton(onClick = { /* TODO: Открыть меню опций */ }) {
                Icon(Icons.Filled.MoreVert, contentDescription = "Опции")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        )
    )
}

// 4. Дополнительные Элементы: Кнопка Добавления (FAB)
@Composable
fun NoteFAB(onAddNoteClick: () -> Unit) {
    // ЛОГИКА FAB: Теперь FAB вызывает переданный колбэк
    FloatingActionButton(onClick = onAddNoteClick) {
        Icon(Icons.Filled.Add, contentDescription = "Добавить заметку")
    }
}

// 4. Дополнительные Элементы: Состояние Пустого Экрана
@Composable
fun EmptyState(paddingValues: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        Text("У вас пока нет заметок. Нажмите + чтобы создать первую!", color = Color.Gray)
    }
}

// 3. Основное Содержимое (Заметки)
@Composable
fun NotesListContent(
    notes: List<Note>,
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
        items(notes, key = { it.id }) { note -> // Добавлен ключ для лучшей производительности
            NoteCard(note, onNoteClick, onDeleteClick)
        }
    }
}

// Карточка Заметки (Простая реализация)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteCard(
    note: Note,
    onNoteClick: (Note) -> Unit,
    onDeleteClick: (Note) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp)
            .padding(4.dp),
        // **ЛОГИКА КНОПКИ: Нажатие на карточку**
        onClick = { onNoteClick(note) },
        colors = CardDefaults.cardColors(
            containerColor = Color(note.color).copy(alpha = 0.3f)
        )
    ) {
        // ... (содержимое NoteCard остается без изменений)
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = note.title.ifEmpty { "(Без заголовка)" },
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium,
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
                    text = "Создано: ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(java.util.Date(note.timestamp))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // TODO: Добавить иконку "Удалить" здесь, чтобы onDeleteClick работал
                // В текущем коде onDeleteClick не вызывается.
                // Мы сделаем это на следующем шаге.
            }
        }
    }
}