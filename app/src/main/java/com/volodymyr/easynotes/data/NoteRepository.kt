package com.volodymyr.easynotes.data
import kotlinx.coroutines.flow.Flow

class NoteRepository(
    private val noteDao: NoteDao,
    private val attachmentDao: AttachmentDao
) {

    val allNotes: Flow<List<Note>> = noteDao.getAllNotes()

    suspend fun insert(note: Note): Long {
        return noteDao.insert(note)
    }

    suspend fun update(note: Note) {
        noteDao.update(note)
    }

    suspend fun delete(note: Note) {
        noteDao.delete(note)
    }

    suspend fun deleteAll() {
        noteDao.deleteAll()
    }

    suspend fun getNoteById(noteId: Int): Note? {
        return noteDao.getNoteById(noteId)
    }

    fun getAttachmentsForNote(noteId: Int): Flow<List<Attachment>> {
        return attachmentDao.getAttachmentsForNote(noteId)
    }

    suspend fun insertAttachment(attachment: Attachment) {
        attachmentDao.insert(attachment)
    }

    suspend fun deleteAttachment(attachment: Attachment) {
        attachmentDao.delete(attachment)
    }
}