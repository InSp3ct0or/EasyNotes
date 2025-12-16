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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.volodymyr.easynotes.viewmodel.NoteViewModel
import com.volodymyr.easynotes.viewmodel.NoteViewModelFactory
import com.volodymyr.easynotes.ui.theme.EasyNotesTheme
import com.volodymyr.easynotes.EasyNotesApplication
import com.volodymyr.easynotes.NoteDestinations.DRAWING_ROUTE
import com.volodymyr.easynotes.NoteDestinations.NOTE_DETAIL_FULL_ROUTE
import com.volodymyr.easynotes.NoteDestinations.NOTE_DETAIL_ROUTE
import com.volodymyr.easynotes.NoteDestinations.NOTES_ROUTE
import com.volodymyr.easynotes.NoteDestinations.NOTE_ID_KEY

// Импорт NotesScreen
import com.volodymyr.easynotes.NotesScreen
// НОВЫЙ ИМПОРТ: NoteDetailScreen
import com.volodymyr.easynotes.NoteDetailScreen
import com.volodymyr.easynotes.data.Note


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val application = application as EasyNotesApplication

        setContent {
            EasyNotesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val noteViewModel: NoteViewModel = viewModel(
                        factory = NoteViewModelFactory(application.repository)
                    )

                    NoteApp(noteViewModel = noteViewModel)
                }
            }
        }
    }
}


@Composable
fun NoteApp(
    noteViewModel: NoteViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    val notes: List<Note> by noteViewModel.allNotes.observeAsState(initial = emptyList())

    NavHost(
        navController = navController,
        startDestination = NOTES_ROUTE,
        modifier = modifier
    ) {
        // 1. Компоновщик для экрана списка заметок
        composable(NOTES_ROUTE) {
            NotesScreen(
                notes = notes,
                onNoteClick = { note ->
                    navController.navigate("$NOTE_DETAIL_ROUTE/${note.id}")
                },
                onDeleteClick = { noteViewModel.delete(it) },
                onAddNoteClick = {
                    navController.navigate("$NOTE_DETAIL_ROUTE/0")
                }
            )
        }

        // 2. Компоновщик для экрана добавления/редактирования (ТЕПЕРЬ С NoteDetailScreen)
        composable(
            route = NOTE_DETAIL_FULL_ROUTE,
            arguments = listOf(navArgument(NOTE_ID_KEY) { type = NavType.IntType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getInt(NOTE_ID_KEY) ?: 0

            NoteDetailScreen(
                noteId = noteId,
                navController = navController,
                noteViewModel = noteViewModel
            )
        }

        // 3. Новый компоновщик для экрана рисования
        composable(DRAWING_ROUTE) {
            DrawingScreen()
        }
    }
}