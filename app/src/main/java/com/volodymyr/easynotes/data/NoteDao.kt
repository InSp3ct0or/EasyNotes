package com.volodymyr.easynotes.data
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow // Используем Flow для получения данных в реальном времени

@Dao
interface NoteDao {

    // Для получения всех заметок, отсортированных по дате (самые новые сверху)
    @Query("SELECT * FROM note_table ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<Note>>

    // Вставка новой заметки. IGNORE - если заметка с таким id уже есть (хотя id автогенерируется)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(note: Note): Long // Long - это ID вставленной заметки

    // Обновление заметки
    @Update
    suspend fun update(note: Note)

    // Удаление заметки
    @Delete
    suspend fun delete(note: Note)

    // Удаление всех заметок
    @Query("DELETE FROM note_table")
    suspend fun deleteAll()
}