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


import androidx.compose.runtime.livedata.observeAsState 
import androidx.lifecycle.viewmodel.compose.viewModel 



import com.volodymyr.easynotes.data.Note
import com.volodymyr.easynotes.viewmodel.NoteViewModel
import com.volodymyr.easynotes.viewmodel.NoteViewModelFactory


import com.volodymyr.easynotes.ui.theme.EasyNotesTheme


import com.volodymyr.easynotes.EasyNotesApplication


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

    val notes: List<Note> by noteViewModel.allNotes.observeAsState(initial = emptyList())


    NotesScreen(
        notes = notes,
        onNoteClick = {  },
        onDeleteClick = { noteViewModel.delete(it) }
    )
}


@Composable
fun NotesScreen(
    notes: List<Note>,
    onNoteClick: (Note) -> Unit,
    onDeleteClick: (Note) -> Unit
) {





}