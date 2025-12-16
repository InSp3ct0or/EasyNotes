package com.volodymyr.easynotes

import android.app.Application

import com.volodymyr.easynotes.data.NoteDatabase
import com.volodymyr.easynotes.data.NoteRepository


class EasyNotesApplication : Application() {


    private val database by lazy {

        NoteDatabase.getDatabase(this)
    }


    val repository by lazy {

        NoteRepository(
            noteDao = database.noteDao(),
            attachmentDao = database.attachmentDao() 
        )
    }
}