package com.volodymyr.easynotes.data
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "note_table")
data class Note(

    // PrimaryKey с автогенерацией (autogenerate = true) обязателен
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(), // Для сортировки
    val color: Int, // Например, можно хранить R.color.red или просто Int код цвета
    val drawingPathsJson: String? = null // JSON-строка с данными о рисунке
)