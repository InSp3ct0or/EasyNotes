package com.volodymyr.easynotes.viewmodel

// ДОБАВЬТЕ ЭТИ ИМПОРТЫ
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.volodymyr.easynotes.data.NoteRepository

/**
 * Фабрика для NoteViewModel, принимающая NoteRepository.
 */
class NoteViewModelFactory(private val repository: NoteRepository) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
            // Возвращаем экземпляр NoteViewModel с репозиторием
            return NoteViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}