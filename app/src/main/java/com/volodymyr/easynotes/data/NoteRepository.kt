package com.volodymyr.easynotes.data
import kotlinx.coroutines.flow.Flow

/**
 * Репозиторий скрывает детали реализации базы данных от ViewModel.
 * Он управляет запросами к базе данных и разрешает конфликты между различными источниками данных.
 */
// ИЗМЕНЕНИЕ 1: Добавлен attachmentDao в конструктор
class NoteRepository(
    private val noteDao: NoteDao,
    private val attachmentDao: AttachmentDao // НОВЫЙ DAO
) {

    // ==== МЕТОДЫ ДЛЯ ЗАМЕТОК (NOTE) ====

    val allNotes: Flow<List<Note>> = noteDao.getAllNotes()

    suspend fun insert(note: Note): Long { // ИЗМЕНЕНИЕ: Возвращаем ID вставленной заметки
        // При вставке NoteDao.insert() должен возвращать ID вставленной строки,
        // чтобы мы могли использовать его для привязки вложений.
        return noteDao.insert(note)
    }

    suspend fun update(note: Note) {
        noteDao.update(note)
    }

    suspend fun delete(note: Note) {
        noteDao.delete(note)
        // Вложения будут удалены автоматически благодаря ForeignKey.CASCADE,
        // но здесь можно добавить логику удаления самих файлов из хранилища.
    }

    suspend fun deleteAll() {
        noteDao.deleteAll()
    }

    // ==== НОВЫЕ МЕТОДЫ ДЛЯ ВЛОЖЕНИЙ (ATTACHMENTS) ====

    /**
     * Получение всех вложений для конкретной заметки.
     */
    fun getAttachmentsForNote(noteId: Int): Flow<List<Attachment>> {
        return attachmentDao.getAttachmentsForNote(noteId)
    }

    /**
     * Добавление нового вложения к заметке.
     */
    suspend fun insertAttachment(attachment: Attachment) {
        attachmentDao.insert(attachment)
    }

    /**
     * Удаление конкретного вложения.
     * Здесь можно добавить логику удаления самого файла из хранилища.
     */
    suspend fun deleteAttachment(attachment: Attachment) {
        // TODO: Добавить логику удаления файла по пути attachment.filePath
        attachmentDao.delete(attachment)
    }
}