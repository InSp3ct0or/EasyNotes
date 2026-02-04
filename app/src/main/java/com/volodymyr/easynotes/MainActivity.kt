package com.volodymyr.easynotes

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.volodymyr.easynotes.data.Note
import com.volodymyr.easynotes.ui.theme.EasyNotesTheme
import com.volodymyr.easynotes.viewmodel.NoteViewModel
import com.volodymyr.easynotes.viewmodel.NoteViewModelFactory
import com.volodymyr.easynotes.viewmodel.SortOrder
import java.util.Locale

class MainActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        val sharedPreferences = newBase.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val language = sharedPreferences.getString("language", "en") ?: "en"
        val locale = Locale(language)
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val application = application as EasyNotesApplication
        val sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE)

        setContent {
            val noteViewModel: NoteViewModel = viewModel(
                factory = NoteViewModelFactory(application.repository)
            )
            val isDarkMode by noteViewModel.isDarkMode.observeAsState(initial = false)

            EasyNotesTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NoteApp(
                        noteViewModel = noteViewModel,
                        onLanguageChange = {
                            sharedPreferences.edit().putString("language", it).apply()
                            recreate()
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun NoteApp(
    noteViewModel: NoteViewModel,
    modifier: Modifier = Modifier,
    onLanguageChange: (String) -> Unit
) {
    val navController = rememberNavController()

    val notes: List<Note> by noteViewModel.allNotes.observeAsState(initial = emptyList())
    val searchQuery by noteViewModel.searchQuery.observeAsState(initial = "")
    val sortOrder by noteViewModel.sortOrder.observeAsState(initial = SortOrder.BY_DATE)
    val isDarkMode by noteViewModel.isDarkMode.observeAsState(initial = false)

    NavHost(
        navController = navController,
        startDestination = NoteDestinations.NOTES_ROUTE,
        modifier = modifier
    ) {
        composable(NoteDestinations.NOTES_ROUTE) {
            NotesScreen(
                notes = notes,
                searchQuery = searchQuery,
                onSearchQueryChange = { noteViewModel.setSearchQuery(it) },
                sortOrder = sortOrder,
                onSortOrderChange = { noteViewModel.setSortOrder(it) },
                isDarkMode = isDarkMode,
                onDarkModeToggle = { noteViewModel.toggleDarkMode(it) },
                onNoteClick = { note ->
                    navController.navigate("${NoteDestinations.NOTE_DETAIL_ROUTE}/${note.id}")
                },
                onDeleteClick = { noteViewModel.delete(it) },
                onAddNoteClick = {
                    navController.navigate("${NoteDestinations.NOTE_DETAIL_ROUTE}/0")
                },
                onLanguageSelected = onLanguageChange
            )
        }

        composable(
            route = NoteDestinations.NOTE_DETAIL_FULL_ROUTE,
            arguments = listOf(navArgument(NoteDestinations.NOTE_ID_KEY) { type = NavType.IntType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getInt(NoteDestinations.NOTE_ID_KEY) ?: 0

            NoteDetailScreen(
                noteId = noteId,
                navController = navController,
                noteViewModel = noteViewModel
            )
        }

        composable(NoteDestinations.DRAWING_ROUTE) {
            DrawingScreen()
        }
    }
}
