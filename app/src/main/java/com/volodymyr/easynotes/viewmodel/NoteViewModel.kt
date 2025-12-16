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

    // Примечание: .asLiveData(viewModelScope.coroutineContext) устарел.
    // Рекомендуется использовать .asLiveData() без аргументов.
    val allNotes = repository.allNotes.asLiveData()


    // ИСПРАВЛЕНИЕ 6: Изменяем insert, чтобы он возвращал Unit и не требовал колбэка
    fun insert(note: Note) = viewModelScope.launch {
        repository.insert(note)
    }

    fun update(note: Note) = viewModelScope.launch {
        repository.update(note)
    }

    fun delete(note: Note) = viewModelScope.launch {
        repository.delete(note)
    }

    // ИСПРАВЛЕНИЕ 7: Добавлен метод для загрузки заметки по ID
    suspend fun getNoteById(noteId: Int): Note? {
        return repository.getNoteById(noteId)
    }


    fun getAttachmentsForNote(noteId: Int) =
        repository.getAttachmentsForNote(noteId).asLiveData()


    fun insertAttachment(attachment: Attachment) = viewModelScope.launch {
        repository.insertAttachment(attachment)
    }


    fun deleteAttachment(attachment: Attachment) = viewModelScope.launch {
        repository.deleteAttachment(attachment)
    }
}
// NoteViewModelFactory остается неизменным и не включен в ответ.