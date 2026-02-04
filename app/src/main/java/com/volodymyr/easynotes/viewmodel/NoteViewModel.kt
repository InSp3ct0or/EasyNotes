package com.volodymyr.easynotes.viewmodel

import androidx.lifecycle.*
import com.volodymyr.easynotes.data.Note
import com.volodymyr.easynotes.data.NoteRepository
import com.volodymyr.easynotes.data.Attachment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

enum class SortOrder {
    BY_DATE,
    BY_TITLE
}

class NoteViewModel(private val repository: NoteRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asLiveData()

    private val _sortOrder = MutableStateFlow(SortOrder.BY_DATE)
    val sortOrder = _sortOrder.asLiveData()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode = _isDarkMode.asLiveData()

    // Объединяем поток всех заметок, поисковый запрос и порядок сортировки
    val allNotes = combine(
        repository.allNotes,
        _searchQuery,
        _sortOrder
    ) { notes, query, sortOrder ->
        val filtered = if (query.isEmpty()) {
            notes
        } else {
            notes.filter { 
                it.title.contains(query, ignoreCase = true) || 
                it.content.contains(query, ignoreCase = true) 
            }
        }

        when (sortOrder) {
            SortOrder.BY_DATE -> filtered.sortedByDescending { it.timestamp }
            SortOrder.BY_TITLE -> filtered.sortedBy { it.title.lowercase() }
        }
    }.asLiveData(viewModelScope.coroutineContext)

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }

    fun toggleDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
    }

    fun insert(note: Note) = viewModelScope.launch {
        repository.insert(note)
    }

    fun update(note: Note) = viewModelScope.launch {
        repository.update(note)
    }

    fun delete(note: Note) = viewModelScope.launch {
        repository.delete(note)
    }

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
