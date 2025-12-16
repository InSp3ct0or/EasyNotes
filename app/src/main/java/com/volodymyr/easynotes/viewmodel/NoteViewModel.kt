package com.volodymyr.easynotes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.volodymyr.easynotes.data.Note
import com.volodymyr.easynotes.data.NoteRepository
// ИЗМЕНЕНИЕ 1: Импортируем Attachment для новых методов
import com.volodymyr.easynotes.data.Attachment
import kotlinx.coroutines.launch

/**
 * ViewModel для управления данными заметок и вложений.
 */
// ИЗМЕНЕНИЕ 2: Конструктор NoteViewModel не изменился, но теперь Repository содержит логику вложений.
class NoteViewModel(private val repository: NoteRepository) : ViewModel() {

    val allNotes = repository.allNotes.asLiveData(viewModelScope.coroutineContext)

    /**
     * Запускает корутину для вставки новой заметки. Возвращает ID новой заметки.
     */
    fun insert(note: Note, onComplete: (Long) -> Unit) = viewModelScope.launch {
        // ИЗМЕНЕНИЕ 3: Передача колбэка для получения ID вставленной заметки.
        // ID нужен для привязки вложений.
        val newNoteId = repository.insert(note)
        onComplete(newNoteId)
    }

    fun update(note: Note) = viewModelScope.launch {
        repository.update(note)
    }

    fun delete(note: Note) = viewModelScope.launch {
        repository.delete(note)
    }

    // ==== НОВЫЕ МЕТОДЫ ДЛЯ ВЛОЖЕНИЙ (ATTACHMENTS) ====

    /**
     * Получает LiveData со списком вложений для данной заметки.
     */
    fun getAttachmentsForNote(noteId: Int) =
        repository.getAttachmentsForNote(noteId).asLiveData(viewModelScope.coroutineContext)

    /**
     * Добавление вложения к заметке.
     */
    fun insertAttachment(attachment: Attachment) = viewModelScope.launch {
        repository.insertAttachment(attachment)
    }

    /**
     * Удаление вложения.
     */
    fun deleteAttachment(attachment: Attachment) = viewModelScope.launch {
        repository.deleteAttachment(attachment)
    }
}