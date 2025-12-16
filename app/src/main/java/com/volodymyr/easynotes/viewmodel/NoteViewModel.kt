package com.volodymyr.easynotes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.volodymyr.easynotes.data.Note
import com.volodymyr.easynotes.data.NoteRepository

import com.volodymyr.easynotes.data.Attachment
import kotlinx.coroutines.launch



class NoteViewModel(private val repository: NoteRepository) : ViewModel() {

    val allNotes = repository.allNotes.asLiveData(viewModelScope.coroutineContext)

    
    fun insert(note: Note, onComplete: (Long) -> Unit) = viewModelScope.launch {


        val newNoteId = repository.insert(note)
        onComplete(newNoteId)
    }

    fun update(note: Note) = viewModelScope.launch {
        repository.update(note)
    }

    fun delete(note: Note) = viewModelScope.launch {
        repository.delete(note)
    }



    
    fun getAttachmentsForNote(noteId: Int) =
        repository.getAttachmentsForNote(noteId).asLiveData(viewModelScope.coroutineContext)

    
    fun insertAttachment(attachment: Attachment) = viewModelScope.launch {
        repository.insertAttachment(attachment)
    }

    
    fun deleteAttachment(attachment: Attachment) = viewModelScope.launch {
        repository.deleteAttachment(attachment)
    }
}