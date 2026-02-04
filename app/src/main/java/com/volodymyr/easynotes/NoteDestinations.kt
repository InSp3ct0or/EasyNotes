package com.volodymyr.easynotes

object NoteDestinations {
    const val NOTES_ROUTE = "notes_list"
    const val NOTE_DETAIL_ROUTE = "note_detail"
    const val NOTE_ID_KEY = "noteId"
    const val NOTE_DETAIL_FULL_ROUTE = "$NOTE_DETAIL_ROUTE/{$NOTE_ID_KEY}"
    const val DRAWING_ROUTE = "drawing"
}