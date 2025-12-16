package com.volodymyr.easynotes.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

import com.volodymyr.easynotes.data.Note


@Entity(
    tableName = "attachment_table",
    foreignKeys = [
        ForeignKey(
            entity = Note::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE 
        )
    ]
)
data class Attachment(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val noteId: Int,             
    val filePath: String,        
    val mimeType: String,        
    val fileName: String? = null 
)