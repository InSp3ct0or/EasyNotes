package com.volodymyr.easynotes.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AttachmentDao {

    // Вставка нового вложения
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(attachment: Attachment)

    // Удаление вложения
    @Delete
    suspend fun delete(attachment: Attachment)

    // Получение всех вложений для конкретной заметки
    @Query("SELECT * FROM attachment_table WHERE noteId = :noteId ORDER BY id ASC")
    fun getAttachmentsForNote(noteId: Int): Flow<List<Attachment>>
}