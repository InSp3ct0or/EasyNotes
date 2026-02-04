package com.volodymyr.easynotes.data
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {


    @Query("SELECT * FROM note_table ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<Note>>


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(note: Note): Long


    @Update
    suspend fun update(note: Note)


    @Delete
    suspend fun delete(note: Note)


    @Query("DELETE FROM note_table")
    suspend fun deleteAll()

    @Query("SELECT * FROM note_table WHERE id = :noteId")
    suspend fun getNoteById(noteId: Int): Note?
}