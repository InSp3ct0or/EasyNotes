package com.volodymyr.easynotes

object NoteDestinations {
    // Главный экран со списком заметок
    const val NOTES_ROUTE = "notes_list"

    // Экран добавления/редактирования заметки.
    // Аргумент {noteId} указывает, что это может быть новый (id=0) или существующий (id>0) экран.
    const val NOTE_DETAIL_ROUTE = "note_detail"

    // Ключ для аргумента, который передает ID заметки
    const val NOTE_ID_KEY = "noteId"

    // Полный путь для экрана деталей/редактирования (с аргументом)
    const val NOTE_DETAIL_FULL_ROUTE = "$NOTE_DETAIL_ROUTE/{$NOTE_ID_KEY}"

    // Новый маршрут для экрана рисования
    const val DRAWING_ROUTE = "drawing"
}