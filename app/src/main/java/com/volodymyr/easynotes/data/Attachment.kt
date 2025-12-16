package com.volodymyr.easynotes.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
// НОВОЕ ИСПРАВЛЕНИЕ: ЯВНЫЙ ИМПОРТ СУЩНОСТИ NOTE
import com.volodymyr.easynotes.data.Note

// Сущность для хранения информации о вложенных файлах (фото, PDF, TXT и т.д.)
@Entity(
    tableName = "attachment_table",
    foreignKeys = [
        ForeignKey(
            entity = Note::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE // Вложения удаляются при удалении заметки
        )
    ]
)
data class Attachment(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val noteId: Int,             // ID заметки, к которой относится вложение (ForeignKey)
    val filePath: String,        // URI или путь к файлу в хранилище устройства
    val mimeType: String,        // MIME-тип файла (например, "image/jpeg", "application/pdf")
    val fileName: String? = null // Имя файла для отображения
)