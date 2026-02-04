package com.volodymyr.easynotes.data
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "note_table")
data class Note(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val color: Int,
    val drawingPathsJson: String? = null,
    val imagePath: String? = null,
    val textColor: Int = 0,
    val fontSize: Float = 16f
)