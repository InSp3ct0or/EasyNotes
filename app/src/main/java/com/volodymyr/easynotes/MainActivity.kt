package com.volodymyr.easynotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState

import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

// ... (другие импорты)
import androidx.compose.runtime.livedata.observeAsState // Для observeAsState
import androidx.lifecycle.viewmodel.compose.viewModel // Для функции viewModel()
// ...

// Импорт классов данных и viewmodel
import com.volodymyr.easynotes.data.Note
import com.volodymyr.easynotes.viewmodel.NoteViewModel
import com.volodymyr.easynotes.viewmodel.NoteViewModelFactory

// Импорт вашей темы
import com.volodymyr.easynotes.ui.theme.EasyNotesTheme

// НОВОЕ ИСПРАВЛЕНИЕ: Явный импорт класса Application
import com.volodymyr.easynotes.EasyNotesApplication


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Получаем экземпляр Application для доступа к Repository
        val application = application as EasyNotesApplication

        setContent {
            EasyNotesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Создаем ViewModel с помощью нашей Factory
                    val noteViewModel: NoteViewModel = viewModel(
                        factory = NoteViewModelFactory(application.repository)
                    )

                    // Передаем ViewModel в наш главный UI-компонент
                    NoteApp(noteViewModel = noteViewModel)
                }
            }
        }
    }
}

/**
 * Главный контейнер для Compose UI
 */
@Composable
fun NoteApp(
    noteViewModel: NoteViewModel,
    modifier: Modifier = Modifier
) {
    // Наблюдаем за LiveData из ViewModel и получаем ее состояние в Compose
    val notes: List<Note> by noteViewModel.allNotes.observeAsState(initial = emptyList())

    // Здесь будет размещен ваш главный экран со списком заметок и кнопкой добавления
    NotesScreen(
        notes = notes,
        onNoteClick = { /* TODO: Открыть экран редактирования */ },
        onDeleteClick = { noteViewModel.delete(it) }
    )
}

/**
 * ЗАГЛУШКА: Создайте этот файл, чтобы определить ваш основной экран с заметками.
 */
@Composable
fun NotesScreen(
    notes: List<Note>,
    onNoteClick: (Note) -> Unit,
    onDeleteClick: (Note) -> Unit
) {
    // TODO: Реализация списка заметок (например, с помощью LazyColumn)
    // Сейчас это просто заглушка

    // Пример вывода для проверки (требуется import androidx.compose.material3.Text, если вы его используете):
    // Text("Всего заметок: ${notes.size}")
}