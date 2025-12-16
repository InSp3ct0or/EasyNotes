package com.volodymyr.easynotes

import android.app.Application
// Убедитесь, что пути правильные, исходя из структуры папок:
import com.volodymyr.easynotes.data.NoteDatabase
import com.volodymyr.easynotes.data.NoteRepository

// Создаем приложение, которое предоставляет доступ к базе данных и репозиторию
class EasyNotesApplication : Application() {

    // Ленивая инициализация базы данных
    private val database by lazy {
        // Вызов статического метода Room для получения экземпляра базы данных
        NoteDatabase.getDatabase(this)
    }

    // Ленивая инициализация репозитория, используя DAO из базы данных
    val repository by lazy {
        // ИЗМЕНЕНИЕ: Теперь NoteRepository требует два DAO.
        NoteRepository(
            noteDao = database.noteDao(),
            attachmentDao = database.attachmentDao() // НОВЫЙ DAO
        )
    }
}