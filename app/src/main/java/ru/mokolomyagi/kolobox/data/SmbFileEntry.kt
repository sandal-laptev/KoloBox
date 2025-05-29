package ru.mokolomyagi.kolobox.data

data class SmbFileEntry(
    val name: String,
    val isDirectory: Boolean,
    val size: Long
)