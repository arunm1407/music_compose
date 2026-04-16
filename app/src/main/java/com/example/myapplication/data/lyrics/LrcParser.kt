package com.example.myapplication.data.lyrics

import com.example.myapplication.data.LyricLine

object LrcParser {

    private val TIMESTAMP_REGEX = Regex("""\[(\d{2}):(\d{2})\.(\d{2,3})]""")

    fun parse(lrcContent: String): List<LyricLine> {
        val lines = mutableListOf<LyricLine>()

        lrcContent.lines().forEach { line ->
            val matches = TIMESTAMP_REGEX.findAll(line)
            val text = TIMESTAMP_REGEX.replace(line, "").trim()

            if (text.isNotBlank()) {
                matches.forEach { match ->
                    val minutes = match.groupValues[1].toLongOrNull() ?: 0L
                    val seconds = match.groupValues[2].toLongOrNull() ?: 0L
                    val centiseconds = match.groupValues[3].let { cs ->
                        val value = cs.toLongOrNull() ?: 0L
                        if (cs.length == 2) value * 10 else value
                    }
                    val timestampMs = (minutes * 60 + seconds) * 1000 + centiseconds
                    lines.add(LyricLine(timestampMs = timestampMs, text = text))
                }
            }
        }

        return lines.sortedBy { it.timestampMs }
    }
}
