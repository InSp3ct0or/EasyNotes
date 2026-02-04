package com.volodymyr.easynotes.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class TextSpanData(
    val start: Int,
    val end: Int,
    val color: Int? = null,
    val isBold: Boolean = false,
    val isItalic: Boolean = false
)

data class FormattedTextData(
    val text: String,
    val spans: List<TextSpanData>
)

object TextFormatConverter {
    private val gson = Gson()

    fun toJson(annotatedString: AnnotatedString): String {
        val spans = annotatedString.spanStyles.map { range ->
            TextSpanData(
                start = range.start,
                end = range.end,
                color = range.item.color.takeIf { it != Color.Unspecified }?.toArgb(),
                isBold = range.item.fontWeight == FontWeight.Bold,
                isItalic = range.item.fontStyle == FontStyle.Italic
            )
        }
        return gson.toJson(FormattedTextData(annotatedString.text, spans))
    }

    fun fromJson(json: String?): AnnotatedString {
        if (json.isNullOrEmpty()) return AnnotatedString("")
        
        return try {
            val data = gson.fromJson(json, FormattedTextData::class.java)
            val builder = AnnotatedString.Builder(data.text)
            data.spans.forEach { span ->
                builder.addStyle(
                    style = SpanStyle(
                        color = span.color?.let { Color(it) } ?: Color.Unspecified,
                        fontWeight = if (span.isBold) FontWeight.Bold else FontWeight.Normal,
                        fontStyle = if (span.isItalic) FontStyle.Italic else FontStyle.Normal
                    ),
                    start = span.start,
                    end = span.end
                )
            }
            builder.toAnnotatedString()
        } catch (e: Exception) {
            // Fallback to plain text if JSON is invalid or it's an old note
            AnnotatedString(json)
        }
    }
}
