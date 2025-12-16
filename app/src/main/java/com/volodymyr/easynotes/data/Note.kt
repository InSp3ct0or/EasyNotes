package com.volodymyr.easynotes.data
import androidx.room.Database
import androidx.room.RoomDatabase
import android.content.Context
import androidx.room.Room

// ИЗМЕНЕНИЕ 1: Добавлена новая сущность Attachment::class
// ИЗМЕНЕНИЕ 2: Версия базы данных увеличена с 1 до 2
@Database(entities = [Note::class, Attachment::class], version = 2, exportSchema = false)
abstract class NoteDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao

    // ИЗМЕНЕНИЕ 3: НОВАЯ функция для доступа к DAO вложений
    abstract fun attachmentDao(): AttachmentDao

    companion object {
        // Singleton предотвращает создание нескольких экземпляров базы данных
        @Volatile
        private var INSTANCE: NoteDatabase? = null

        fun getDatabase(context: Context): NoteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoteDatabase::class.java,
                    "note_database"
                )
                    // ВАЖНО: При изменении версии требуется миграция.
                    // Для первого запуска мы просто разрешаем разрушать и пересоздавать БД.
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}