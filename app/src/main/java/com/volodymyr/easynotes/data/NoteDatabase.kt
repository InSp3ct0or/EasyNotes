package com.volodymyr.easynotes.data
import androidx.room.Database
import androidx.room.RoomDatabase
import android.content.Context
import androidx.room.Room



@Database(entities = [Note::class, Attachment::class], version = 4, exportSchema = false)
abstract class NoteDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao


    abstract fun attachmentDao(): AttachmentDao

    companion object {

        @Volatile
        private var INSTANCE: NoteDatabase? = null

        fun getDatabase(context: Context): NoteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoteDatabase::class.java,
                    "note_database"
                )


                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
