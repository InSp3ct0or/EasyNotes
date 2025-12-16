package com.volodymyr.easynotes.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AttachmentDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(attachment: Attachment)


    @Delete
    suspend fun delete(attachment: Attachment)


    @Query("SELECT * FROM attachment_table WHERE noteId = :noteId ORDER BY id ASC")
    fun getAttachmentsForNote(noteId: Int): Flow<List<Attachment>>
}