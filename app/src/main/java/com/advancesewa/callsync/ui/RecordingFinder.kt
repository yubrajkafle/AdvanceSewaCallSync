package com.advancesewa.callsync.ui

import java.io.File
import kotlin.math.abs

object RecordingFinder {

    private val candidateDirs = listOf(
        "/storage/emulated/0/Call/Recordings",
        "/storage/emulated/0/Sounds/Call",
        "/storage/emulated/0/Recordings/Call",
        "/storage/emulated/0/Samsung/Call/Recordings"
    )

    data class FileInfo(val file: File, val nameLower: String, val lastModified: Long)

    private fun scan(): List<FileInfo> {
        val list = mutableListOf<FileInfo>()
        candidateDirs.forEach { path ->
            val dir = File(path)
            if (dir.exists() && dir.isDirectory) {
                dir.listFiles()?.forEach { f ->
                    if (f.isFile && f.extension.lowercase() in listOf("m4a","mp4","aac","3gp","amr","wav")) {
                        list += FileInfo(f, f.name.lowercase(), f.lastModified())
                    }
                }
            }
        }
        return list.sortedByDescending { it.lastModified }
    }

    fun hasRecording(number: String?, callStartMs: Long, durationSec: Long): Boolean {
        val cache = scan()
        if (cache.isEmpty()) return false
        val endMs = callStartMs + (durationSec * 1000)
        val digits = (number ?: "").filter { it.isDigit() }
        val last7 = if (digits.length >= 7) digits.takeLast(7) else digits
        if (last7.isNotEmpty()) {
            cache.firstOrNull { it.nameLower.contains(last7) }?.let { return true }
        }
        val window = 3 * 60 * 1000L
        cache.firstOrNull { abs(it.lastModified - endMs) <= window }?.let { return true }
        return false
    }
}
